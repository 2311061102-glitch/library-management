package vn.edu.crs.librarymanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import vn.edu.crs.librarymanagement.entity.Book;
import vn.edu.crs.librarymanagement.entity.BorrowRecord;
import vn.edu.crs.librarymanagement.entity.Fine;
import vn.edu.crs.librarymanagement.entity.BookCopy;
import vn.edu.crs.librarymanagement.entity.LoanRenewal;
import vn.edu.crs.librarymanagement.entity.Reservation;
import vn.edu.crs.librarymanagement.entity.User;
import vn.edu.crs.librarymanagement.repository.BookRepository;
import vn.edu.crs.librarymanagement.repository.BorrowRecordRepository;
import vn.edu.crs.librarymanagement.repository.FineRepository;
import vn.edu.crs.librarymanagement.repository.UserRepository;
import vn.edu.crs.librarymanagement.repository.BookCopyRepository;
import vn.edu.crs.librarymanagement.repository.LoanRenewalRepository;
import vn.edu.crs.librarymanagement.repository.ReservationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BorrowService {

    private static final String BORROWING = "BORROWING";
    private static final String RETURNED = "RETURNED";

    @Value("${library.max-books-per-user}")
    private int maxBooksPerUser;

    @Value("${library.borrow-duration-days}")
    private int borrowDurationDays;

    @Value("${library.max-renew-count}")
    private int maxRenewCount;

    @Value("${library.renew-extra-days}")
    private int renewExtraDays;

    @Value("${library.fine-per-day}")
    private long finePerDay;

    @Value("${library.max-overdue-days:0}")
    private int maxOverdueDays;

    @Value("${library.reservation-hold-days:3}")
    private int reservationHoldDays;

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final FineRepository fineRepository;
    private final BookCopyRepository bookCopyRepository;
    private final ReservationRepository reservationRepository;
    private final LoanRenewalRepository loanRenewalRepository;

    public BorrowService(BorrowRecordRepository borrowRecordRepository, BookRepository bookRepository,
                         UserRepository userRepository, FineRepository fineRepository,
                         BookCopyRepository bookCopyRepository, ReservationRepository reservationRepository,
                         LoanRenewalRepository loanRenewalRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.fineRepository = fineRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.reservationRepository = reservationRepository;
        this.loanRenewalRepository = loanRenewalRepository;
    }

    // ===== Tạo phiếu mượn mới =====
    @Transactional
    public BorrowRecord borrowBook(Long userId, Long bookId) {
        if (userId == null || bookId == null) {
            throw new IllegalArgumentException("userId va bookId khong duoc de trong");
        }

        // Khóa user và book trong cùng transaction để hai request đồng thời
        // không cùng vượt qua các giới hạn nghiệp vụ.
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay nguoi dung"));
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay sach"));
        validateMember(user);

        // Ràng buộc 1: số sách đang mượn không vượt giới hạn
        long currentBorrowing = borrowRecordRepository.countByUserIdAndStatus(userId, BORROWING);
        if (currentBorrowing >= maxBooksPerUser) {
            throw new IllegalStateException("Ban da muon toi da " + maxBooksPerUser + " cuon sach");
        }

        // Ràng buộc 2: không được mượn thêm nếu còn phiếu phạt chưa thanh toán
        boolean hasUnpaidFine = fineRepository.existsByBorrowRecord_User_IdAndStatus(userId, "UNPAID");
        if (hasUnpaidFine) {
            throw new IllegalStateException("Vui long thanh toan het phi phat truoc khi muon sach moi");
        }

        long overdueLoans = borrowRecordRepository.findByUserIdAndStatus(userId, BORROWING).stream()
                .filter(record -> record.getDueDate().isBefore(LocalDateTime.now()))
                .count();
        if (overdueLoans > maxOverdueDays) {
            throw new IllegalStateException("Ban dang co sach qua han, khong the muon them");
        }

        // Không cho phép một người giữ hai phiếu mượn đang hoạt động cho cùng một sách.
        if (borrowRecordRepository.existsByUserIdAndBookIdAndStatus(userId, bookId, BORROWING)) {
            throw new IllegalStateException("Ban dang muon sach nay");
        }

        ensureCopies(book);
        List<BookCopy> availableCopies = bookCopyRepository.findAvailableForUpdate(bookId);
        Reservation head = reservationRepository.findWaitingForUpdate(bookId).stream().findFirst().orElse(null);
        if (head != null && !head.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Sach dang duoc giu cho nguoi dat truoc dau tien");
        }
        if (availableCopies.isEmpty()) {
            throw new IllegalStateException("Sach da het, khong the muon");
        }

        BookCopy copy = availableCopies.get(0);
        copy.setStatus(BookCopy.BORROWED);
        bookCopyRepository.save(copy);
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // Tạo phiếu mượn
        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setBookCopy(copy);
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(borrowDurationDays));
        record.setRenewCount(0);
        record.setStatus(BORROWING);

        BorrowRecord saved = borrowRecordRepository.save(record);
        if (head != null && head.getUser().getId().equals(userId)) {
            head.setStatus(Reservation.FULFILLED);
            reservationRepository.save(head);
        }
        return saved;
    }

    public Map<String, Object> checkBorrowEligibility(Long userId, Long bookId) {
        if (userId == null || bookId == null) {
            throw new IllegalArgumentException("userId va bookId khong duoc de trong");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay nguoi dung"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay sach"));
        validateMember(user);

        long currentBorrowing = borrowRecordRepository.countByUserIdAndStatus(userId, BORROWING);
        boolean hasUnpaidFine = fineRepository.existsByBorrowRecord_User_IdAndStatus(userId, "UNPAID");
        boolean alreadyBorrowing = borrowRecordRepository.existsByUserIdAndBookIdAndStatus(userId, bookId, BORROWING);
        boolean available = book.getAvailableCopies() != null && book.getAvailableCopies() > 0;
        boolean hasOverdue = borrowRecordRepository.findByUserIdAndStatus(userId, BORROWING).stream()
                .anyMatch(record -> record.getDueDate().isBefore(LocalDateTime.now()));
        boolean eligible = currentBorrowing < maxBooksPerUser
                && !hasUnpaidFine
                && !hasOverdue
                && !alreadyBorrowing
                && available;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("eligible", eligible);
        result.put("userId", user.getId());
        result.put("bookId", book.getId());
        result.put("bookTitle", book.getTitle());
        result.put("availableCopies", book.getAvailableCopies());
        result.put("totalCopies", book.getTotalCopies());
        result.put("currentBorrowing", currentBorrowing);
        result.put("maxBooksPerUser", maxBooksPerUser);
        result.put("borrowDurationDays", borrowDurationDays);
        result.put("dueDate", LocalDateTime.now().plusDays(borrowDurationDays));
        result.put("hasUnpaidFine", hasUnpaidFine);
        result.put("hasOverdue", hasOverdue);
        result.put("alreadyBorrowing", alreadyBorrowing);
        result.put("reason", eligibilityReason(currentBorrowing, hasUnpaidFine, hasOverdue, alreadyBorrowing, available));
        return result;
    }

    private String eligibilityReason(long currentBorrowing, boolean hasUnpaidFine,
                                    boolean hasOverdue, boolean alreadyBorrowing, boolean available) {
        if (currentBorrowing >= maxBooksPerUser) {
            return "Ban da dat gioi han " + maxBooksPerUser + " cuon sach dang muon";
        }
        if (hasUnpaidFine) {
            return "Ban con phieu phat chua thanh toan";
        }
        if (hasOverdue) {
            return "Ban dang co sach qua han";
        }
        if (alreadyBorrowing) {
            return "Ban dang muon sach nay";
        }
        if (!available) {
            return "Sach da het ban";
        }
        return "Du dieu kien muon sach";
    }

    // ===== Gia hạn phiếu mượn =====
    public BorrowRecord renewBorrow(Long borrowId) {
        return renewBorrow(borrowId, null);
    }

    @Transactional
    public BorrowRecord renewBorrow(Long borrowId, Long userId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay phieu muon"));

        checkOwner(record, userId);
        validateMember(record.getUser());
        if (!BORROWING.equals(record.getStatus())) {
            throw new IllegalStateException("Phieu muon nay da tra, khong the gia han");
        }
        if (LocalDateTime.now().isAfter(record.getDueDate())) {
            throw new IllegalStateException("Phieu muon da qua han, khong the gia han");
        }
        if (record.getRenewCount() == null || record.getRenewCount() >= maxRenewCount) {
            throw new IllegalStateException("Da het luot gia han cho phieu muon nay");
        }
        if (borrowRecordRepository.existsByBookIdAndStatus(record.getBook().getId(), BORROWING)
                && reservationRepository.findByBookIdAndStatusOrderByReservedAtAsc(record.getBook().getId(), Reservation.WAITING)
                .stream().anyMatch(r -> !r.getUser().getId().equals(record.getUser().getId()))) {
            throw new IllegalStateException("Sach da co nguoi dat truoc, khong the gia han");
        }
        if (fineRepository.existsByBorrowRecord_User_IdAndStatus(record.getUser().getId(), "UNPAID")) {
            throw new IllegalStateException("Vui long thanh toan het phi phat truoc khi gia han");
        }

        LocalDateTime oldDueDate = record.getDueDate();
        record.setDueDate(record.getDueDate().plusDays(renewExtraDays));
        record.setRenewCount(record.getRenewCount() + 1);
        BorrowRecord saved = borrowRecordRepository.save(record);
        LoanRenewal renewal = new LoanRenewal();
        renewal.setBorrowRecord(saved);
        renewal.setOldDueDate(oldDueDate);
        renewal.setNewDueDate(saved.getDueDate());
        renewal.setRenewedAt(LocalDateTime.now());
        loanRenewalRepository.save(renewal);
        return saved;
    }

    // ===== Trả sách =====
    @Transactional
    public BorrowRecord returnBook(Long borrowId) {
        return processReturn(borrowId, null, "GOOD", null);
    }

    @Transactional
    public BorrowRecord returnBook(Long borrowId, Long userId) {
        return processReturn(borrowId, userId, "GOOD", null);
    }

    private BorrowRecord processReturn(Long borrowId, Long userId, String condition, String note) {
        if (condition == null || condition.isBlank()) {
            condition = "GOOD";
        }
        condition = condition.toUpperCase();
        if (!List.of("GOOD", "DAMAGED", "LOST").contains(condition)) {
            throw new IllegalArgumentException("Tinh trang sach khong hop le");
        }
        BorrowRecord record = borrowRecordRepository.findById(borrowId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay phieu muon"));

        checkOwner(record, userId);
        if (RETURNED.equals(record.getStatus())) {
            throw new IllegalStateException("Phieu muon nay da duoc tra truoc do");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setReturnDate(now);
        record.setStatus(RETURNED);

        BookCopy copy = record.getBookCopy();
        Book book = record.getBook();
        Reservation nextReservation = reservationRepository.findWaitingForUpdate(book.getId())
                .stream().findFirst().orElse(null);
        if (copy != null) {
            if ("DAMAGED".equals(condition)) {
                copy.setStatus(BookCopy.DAMAGED);
            } else if ("LOST".equals(condition)) {
                copy.setStatus(BookCopy.LOST);
            } else if (nextReservation != null) {
                copy.setStatus(BookCopy.RESERVED);
                nextReservation.setStatus(Reservation.READY);
                nextReservation.setExpiresAt(now.plusDays(reservationHoldDays));
                reservationRepository.save(nextReservation);
            } else {
                copy.setStatus(BookCopy.AVAILABLE);
            }
            bookCopyRepository.save(copy);
        }
        if ("GOOD".equals(condition) && nextReservation == null) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
        }
        bookRepository.save(book);

        BorrowRecord saved = borrowRecordRepository.save(record);

        // Nếu trả trễ hạn, tự động tạo duy nhất một phiếu phạt cho lần mượn này.
        if (now.isAfter(record.getDueDate()) && fineRepository.findByBorrowRecord_Id(record.getId()).isEmpty()) {
            long lateDays = ChronoUnit.DAYS.between(record.getDueDate(), now);
            if (lateDays < 1) lateDays = 1; // trễ dù chỉ vài giờ vẫn tính tối thiểu 1 ngày

            Fine fine = new Fine();
            fine.setBorrowRecord(saved);
            fine.setAmount(BigDecimal.valueOf(lateDays * finePerDay));
            fine.setReason("Tra sach tre " + lateDays + " ngay");
            fine.setStatus("UNPAID");
            fine.setCreatedAt(now);
            fineRepository.save(fine);
        }

        if (!"GOOD".equals(condition)) {
            Fine conditionFine = new Fine();
            conditionFine.setBorrowRecord(saved);
            conditionFine.setAmount(book.getPrice() == null ? BigDecimal.valueOf(finePerDay) : book.getPrice());
            conditionFine.setReason(("LOST".equals(condition) ? "Mat sach" : "Hu hong sach")
                    + (note == null || note.isBlank() ? "" : ": " + note));
            conditionFine.setStatus("UNPAID");
            conditionFine.setCreatedAt(now);
            fineRepository.save(conditionFine);
        }

        return saved;
    }

    @Transactional
    public BorrowRecord returnBook(Long borrowId, Long userId, String condition, String note) {
        BorrowRecord record = borrowRecordRepository.findById(borrowId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay phieu muon"));
        checkOwner(record, userId);
        if (condition == null || condition.isBlank()) {
            condition = "GOOD";
        }
        return processReturn(borrowId, userId, condition, note);
    }

    @Transactional
    public BorrowRecord returnByBarcode(String barcode, String condition, String note) {
        if (barcode == null || barcode.isBlank()) {
            throw new IllegalArgumentException("Barcode khong duoc de trong");
        }
        BorrowRecord record = borrowRecordRepository
                .findByBookCopy_BarcodeAndStatus(barcode, BORROWING)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay phieu muon dang mo cho barcode nay"));
        return processReturn(record.getId(), null, condition, note);
    }

    @Transactional
    public Reservation reserveBook(Long userId, Long bookId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay nguoi dung"));
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay sach"));
        validateMember(user);
        if (book.getAvailableCopies() != null && book.getAvailableCopies() > 0) {
            throw new IllegalStateException("Sach dang con, ban co the muon ngay");
        }
        if (reservationRepository.existsByBookIdAndUserIdAndStatus(bookId, userId, Reservation.WAITING)) {
            throw new IllegalStateException("Ban da dat truoc sach nay");
        }
        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setUser(user);
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setStatus(Reservation.WAITING);
        return reservationRepository.save(reservation);
    }

    private void validateMember(User user) {
        if (user.getAccountStatus() != null && !"ACTIVE".equalsIgnoreCase(user.getAccountStatus())) {
            throw new IllegalStateException("Tai khoan doc gia khong o trang thai ACTIVE");
        }
        LocalDateTime now = LocalDateTime.now();
        if (user.getCardExpiresAt() != null && user.getCardExpiresAt().isBefore(now.toLocalDate())) {
            throw new IllegalStateException("The doc gia da het han");
        }
        if (user.getBlockedUntil() != null && !user.getBlockedUntil().isBefore(now.toLocalDate())) {
            throw new IllegalStateException("Tai khoan dang bi khoa");
        }
    }

    private void ensureCopies(Book book) {
        long currentCopies = bookCopyRepository.countByBookIdAndStatus(book.getId(), BookCopy.AVAILABLE)
                + bookCopyRepository.countByBookIdAndStatus(book.getId(), BookCopy.BORROWED)
                + bookCopyRepository.countByBookIdAndStatus(book.getId(), BookCopy.RESERVED)
                + bookCopyRepository.countByBookIdAndStatus(book.getId(), BookCopy.DAMAGED)
                + bookCopyRepository.countByBookIdAndStatus(book.getId(), BookCopy.LOST);
        int target = currentCopies == 0 && book.getAvailableCopies() != null
                ? book.getAvailableCopies()
                : (book.getTotalCopies() == null ? 0 : book.getTotalCopies());
        for (long i = currentCopies; i < target; i++) {
            BookCopy copy = new BookCopy();
            copy.setBook(book);
            copy.setBarcode("BOOK-" + book.getId() + "-" + (i + 1));
            copy.setStatus(BookCopy.AVAILABLE);
            bookCopyRepository.save(copy);
        }
    }

    private void checkOwner(BorrowRecord record, Long userId) {
        if (userId != null && (record.getUser() == null || !userId.equals(record.getUser().getId()))) {
            throw new SecurityException("Ban khong co quyen thao tac phieu muon nay");
        }
    }

    // ===== Xem phiếu mượn của 1 người dùng =====
    public List<BorrowRecord> getBorrowsByUser(Long userId) {
        return borrowRecordRepository.findByUserId(userId);
    }

    // ===== Xem tất cả phiếu mượn (dành cho ADMIN) =====
    public List<BorrowRecord> getAllBorrows() {
        return borrowRecordRepository.findAll();
    }

    public Optional<BorrowRecord> getBorrowById(Long id) {
        return borrowRecordRepository.findById(id);
    }
}