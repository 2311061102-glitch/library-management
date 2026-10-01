package vn.edu.crs.librarymanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.edu.crs.librarymanagement.entity.Book;
import vn.edu.crs.librarymanagement.entity.BorrowRecord;
import vn.edu.crs.librarymanagement.entity.Fine;
import vn.edu.crs.librarymanagement.entity.User;
import vn.edu.crs.librarymanagement.repository.BookRepository;
import vn.edu.crs.librarymanagement.repository.BorrowRecordRepository;
import vn.edu.crs.librarymanagement.repository.FineRepository;
import vn.edu.crs.librarymanagement.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final FineRepository fineRepository;

    public BorrowService(BorrowRecordRepository borrowRecordRepository, BookRepository bookRepository,
                         UserRepository userRepository, FineRepository fineRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.fineRepository = fineRepository;
    }

    // ===== Tạo phiếu mượn mới =====
    public BorrowRecord borrowBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay nguoi dung"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay sach"));

        // Ràng buộc 1: số sách đang mượn không vượt giới hạn
        long currentBorrowing = borrowRecordRepository.findByUserIdAndStatus(userId, BORROWING).size();
        if (currentBorrowing >= maxBooksPerUser) {
            throw new IllegalStateException("Ban da muon toi da " + maxBooksPerUser + " cuon sach");
        }

        // Ràng buộc 2: không được mượn thêm nếu còn phiếu phạt chưa thanh toán
        boolean hasUnpaidFine = fineRepository.existsByBorrowRecord_User_IdAndStatus(userId, "UNPAID");
        if (hasUnpaidFine) {
            throw new IllegalStateException("Vui long thanh toan het phi phat truoc khi muon sach moi");
        }

        // Ràng buộc 3: sách phải còn tồn kho
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("Sach da het, khong the muon");
        }

        // Trừ số bản còn lại
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // Tạo phiếu mượn
        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(borrowDurationDays));
        record.setRenewCount(0);
        record.setStatus(BORROWING);

        return borrowRecordRepository.save(record);
    }

    // ===== Gia hạn phiếu mượn =====
    public BorrowRecord renewBorrow(Long borrowId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay phieu muon"));

        if (!BORROWING.equals(record.getStatus())) {
            throw new IllegalStateException("Phieu muon nay da tra, khong the gia han");
        }
        if (LocalDateTime.now().isAfter(record.getDueDate())) {
            throw new IllegalStateException("Phieu muon da qua han, khong the gia han");
        }
        if (record.getRenewCount() >= maxRenewCount) {
            throw new IllegalStateException("Da het luot gia han cho phieu muon nay");
        }

        record.setDueDate(record.getDueDate().plusDays(renewExtraDays));
        record.setRenewCount(record.getRenewCount() + 1);

        return borrowRecordRepository.save(record);
    }

    // ===== Trả sách =====
    public BorrowRecord returnBook(Long borrowId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay phieu muon"));

        if (RETURNED.equals(record.getStatus())) {
            throw new IllegalStateException("Phieu muon nay da duoc tra truoc do");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setReturnDate(now);
        record.setStatus(RETURNED);

        // Hoàn lại số bản sách còn lại
        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        BorrowRecord saved = borrowRecordRepository.save(record);

        // Nếu trả trễ hạn, tự động tạo phiếu phạt
        if (now.isAfter(record.getDueDate())) {
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

        return saved;
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