package vn.edu.library.borrowservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.library.borrowservice.client.BookClient;
import vn.edu.library.borrowservice.dto.BorrowRecordDTO;
import vn.edu.library.borrowservice.dto.BorrowRequestDTO;
import vn.edu.library.borrowservice.dto.BorrowSummaryDTO;
import vn.edu.library.borrowservice.dto.BorrowAdminSummaryDTO;
import vn.edu.library.borrowservice.entity.BorrowRecord;
import vn.edu.library.borrowservice.entity.Fine;
import vn.edu.library.borrowservice.repository.BorrowRecordRepository;
import vn.edu.library.borrowservice.repository.FineRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final FineRepository fineRepository;
    private final BookClient bookClient;

    @Value("${borrow.loan-days:14}")
    private int loanDays;

    @Value("${borrow.max-active-loans:5}")
    private int maxActiveLoans;

    @Value("${borrow.fine-per-day:5000}")
    private long finePerDay;

    @Value("${borrow.max-renewals:1}")
    private int maxRenewals;

    /**
     * Mượn sách.
     * @param currentUserId userId lấy từ JWT
     * @param librarian     true nếu người gọi là thủ thư (được mượn hộ độc giả khác)
     */
    public BorrowRecordDTO borrow(BorrowRequestDTO dto, Long currentUserId, boolean librarian) {
        Long readerId = currentUserId;
        if (librarian) {
            if (dto.getReaderId() == null) {
                throw new IllegalArgumentException("Thủ thư cần nhập readerId của độc giả");
            }
            readerId = dto.getReaderId();
        }

        if (fineRepository.existsByReaderIdAndPaidFalse(readerId)) {
            throw new IllegalStateException("Độc giả còn khoản phạt chưa thanh toán, không thể mượn thêm");
        }
        if (borrowRecordRepository.countByReaderIdAndStatus(readerId, BorrowRecord.BORROWING) >= maxActiveLoans) {
            throw new IllegalStateException("Đã đạt giới hạn " + maxActiveLoans + " cuốn đang mượn");
        }
        if (borrowRecordRepository.existsByReaderIdAndBookIdAndStatus(
                readerId, dto.getBookId(), BorrowRecord.BORROWING)) {
            throw new IllegalStateException("Độc giả đang mượn cuốn sách này rồi");
        }

        // Bước 1: gọi sang book-service để trừ bản TRƯỚC.
        // Nếu bước này ném exception thì dừng ngay, KHÔNG lưu phiếu mượn.
        String bookTitle = bookClient.reserveCopy(dto.getBookId());

        // Bước 2: chỉ lưu phiếu mượn SAU KHI book-service xác nhận thành công.
        try {
            BorrowRecord record = new BorrowRecord();
            record.setReaderId(readerId);
            record.setBookId(dto.getBookId());
            record.setBookTitle(bookTitle);
            record.setBorrowDate(LocalDateTime.now());
            record.setDueDate(LocalDate.now().plusDays(loanDays));
            record.setStatus(BorrowRecord.BORROWING);
            record.setRenewalCount(0);
            return toDTO(borrowRecordRepository.save(record));
        } catch (RuntimeException e) {
            // Bù trừ (compensation): lưu phiếu thất bại thì hoàn trả bản sách đã trừ ở book-service.
            try {
                bookClient.releaseCopy(dto.getBookId());
            } catch (RuntimeException ignored) {
                // nếu hoàn trả cũng lỗi thì chỉ còn cách đối soát thủ công - giới hạn đã biết của kiến trúc đơn giản này
            }
            throw e;
        }
    }

    /**
     * Trả sách: hoàn bản ở book-service, đóng phiếu mượn và tạo khoản phạt nếu trả trễ.
     */
    @Transactional
    public BorrowRecordDTO returnBook(Long recordId, Long currentUserId, boolean librarian) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phiếu mượn id = " + recordId));

        if (!librarian && !record.getReaderId().equals(currentUserId)) {
            throw new AccessDeniedException("Bạn không có quyền trả phiếu mượn của người khác");
        }
        if (BorrowRecord.RETURNED.equals(record.getStatus())) {
            throw new IllegalStateException("Phiếu mượn này đã được trả trước đó");
        }

        // Gọi sang book-service để hoàn trả bản TRƯỚC khi đổi trạng thái
        bookClient.releaseCopy(record.getBookId());

        LocalDateTime now = LocalDateTime.now();
        record.setReturnDate(now);
        record.setStatus(BorrowRecord.RETURNED);
        borrowRecordRepository.save(record);

        int overdueDays = FineCalculator.overdueDays(record.getDueDate(), now.toLocalDate());
        if (overdueDays > 0) {
            Fine fine = new Fine();
            fine.setBorrowRecord(record);
            fine.setReaderId(record.getReaderId());
            fine.setOverdueDays(overdueDays);
            fine.setAmount(FineCalculator.fineAmount(overdueDays, finePerDay));
            fine.setReason("Trả sách trễ " + overdueDays + " ngày");
            fine.setPaid(false);
            fine.setCreatedAt(now);
            fineRepository.save(fine);
        }
        return toDTO(record);
    }

    @Transactional
    public BorrowRecordDTO renew(Long recordId, Long currentUserId, boolean librarian) {
        BorrowRecord record = find(recordId);
        assertOwnerOrLibrarian(record, currentUserId, librarian);
        if (!BorrowRecord.BORROWING.equals(record.getStatus())) {
            throw new IllegalStateException("Chỉ có thể gia hạn phiếu đang mượn");
        }
        int renewals = record.getRenewalCount() == null ? 0 : record.getRenewalCount();
        if (renewals >= maxRenewals) {
            throw new IllegalStateException("Phiếu mượn đã hết số lần gia hạn");
        }
        if (FineCalculator.overdueDays(record.getDueDate(), LocalDate.now()) > 0) {
            throw new IllegalStateException("Không thể gia hạn sách đã quá hạn");
        }
        if (fineRepository.existsByReaderIdAndPaidFalse(record.getReaderId())) {
            throw new IllegalStateException("Độc giả còn khoản phạt chưa thanh toán, không thể gia hạn");
        }
        record.setDueDate(record.getDueDate().plusDays(loanDays));
        record.setRenewalCount(renewals + 1);
        return toDTO(borrowRecordRepository.save(record));
    }

    public List<BorrowRecordDTO> getMyBorrows(Long readerId, String status, Boolean overdue) {
        return borrowRecordRepository.findByReaderIdOrderByIdDesc(readerId).stream()
                .filter(r -> status == null || status.isBlank() || status.equalsIgnoreCase(r.getStatus()))
                .filter(r -> overdue == null || overdue == isOverdue(r))
                .map(this::toDTO).toList();
    }

    public BorrowSummaryDTO getMySummary(Long readerId) {
        long total = borrowRecordRepository.countByReaderId(readerId);
        long active = borrowRecordRepository.countByReaderIdAndStatus(readerId, BorrowRecord.BORROWING);
        long returned = borrowRecordRepository.countByReaderIdAndStatus(readerId, BorrowRecord.RETURNED);
        long overdue = borrowRecordRepository.findByReaderIdOrderByIdDesc(readerId).stream()
                .filter(this::isOverdue).count();
        return new BorrowSummaryDTO(total, active, returned, overdue,
                fineRepository.countByReaderIdAndPaidFalse(readerId),
                fineRepository.sumAmountByReaderIdAndPaidFalse(readerId));
    }

    public BorrowRecordDTO getById(Long recordId, Long currentUserId, boolean librarian) {
        if (recordId == null || recordId <= 0) {
            throw new IllegalArgumentException("id phiếu mượn phải là số dương");
        }
        BorrowRecord record = find(recordId);
        assertOwnerOrLibrarian(record, currentUserId, librarian);
        return toDTO(record);
    }

    public BorrowAdminSummaryDTO getAdminSummary() {
        List<BorrowRecord> records = borrowRecordRepository.findAll();
        long active = records.stream().filter(r -> BorrowRecord.BORROWING.equals(r.getStatus())).count();
        long returned = records.stream().filter(r -> BorrowRecord.RETURNED.equals(r.getStatus())).count();
        long overdue = records.stream().filter(this::isOverdue).count();
        return new BorrowAdminSummaryDTO(
                records.size(),
                active,
                returned,
                overdue,
                fineRepository.countByPaidFalse(),
                fineRepository.sumAmountByPaidFalse()
        );
    }

    /** Dành cho thủ thư: xem toàn bộ phiếu mượn, lọc tùy chọn theo trạng thái. */
    public Page<BorrowRecordDTO> getAll(String status, Long readerId, Pageable pageable) {
        if (status != null && !status.isBlank()
                && !BorrowRecord.BORROWING.equals(status)
                && !BorrowRecord.RETURNED.equals(status)) {
            throw new IllegalArgumentException("status phải là BORROWING hoặc RETURNED");
        }
        Page<BorrowRecord> page = (status == null || status.isBlank())
                ? (readerId == null ? borrowRecordRepository.findAll(pageable)
                : borrowRecordRepository.findByReaderId(readerId, pageable))
                : (readerId == null ? borrowRecordRepository.findByStatus(status, pageable)
                : borrowRecordRepository.findByReaderIdAndStatus(readerId, status, pageable));
        return page.map(this::toDTO);
    }

    private BorrowRecord find(Long recordId) {
        return borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phiếu mượn id = " + recordId));
    }

    private void assertOwnerOrLibrarian(BorrowRecord record, Long currentUserId, boolean librarian) {
        if (!librarian && !record.getReaderId().equals(currentUserId)) {
            throw new AccessDeniedException("Bạn không có quyền thao tác phiếu mượn của người khác");
        }
    }

    private boolean isOverdue(BorrowRecord record) {
        if (!BorrowRecord.BORROWING.equals(record.getStatus())) {
            return false;
        }
        return FineCalculator.overdueDays(record.getDueDate(), LocalDate.now()) > 0;
    }

    private BorrowRecordDTO toDTO(BorrowRecord r) {
        boolean returned = BorrowRecord.RETURNED.equals(r.getStatus());
        LocalDate asOf = returned && r.getReturnDate() != null
                ? r.getReturnDate().toLocalDate() : LocalDate.now();
        int overdueDays = FineCalculator.overdueDays(r.getDueDate(), asOf);
        int renewalCount = r.getRenewalCount() == null ? 0 : r.getRenewalCount();
        return new BorrowRecordDTO(
                r.getId(), r.getReaderId(), r.getBookId(), r.getBookTitle(),
                r.getBorrowDate(), r.getDueDate(), r.getReturnDate(), r.getStatus(),
                !returned && overdueDays > 0,
                overdueDays,
                FineCalculator.fineAmount(overdueDays, finePerDay),
                renewalCount,
                Math.max(0, maxRenewals - renewalCount)
        );
    }
}
