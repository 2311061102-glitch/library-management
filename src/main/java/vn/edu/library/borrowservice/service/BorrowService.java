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

    public List<BorrowRecordDTO> getMyBorrows(Long readerId) {
        return borrowRecordRepository.findByReaderIdOrderByIdDesc(readerId).stream()
                .map(this::toDTO).toList();
    }

    /** Dành cho thủ thư: xem toàn bộ phiếu mượn, lọc tùy chọn theo trạng thái. */
    public Page<BorrowRecordDTO> getAll(String status, Pageable pageable) {
        Page<BorrowRecord> page = (status == null || status.isBlank())
                ? borrowRecordRepository.findAll(pageable)
                : borrowRecordRepository.findByStatus(status, pageable);
        return page.map(this::toDTO);
    }

    private BorrowRecordDTO toDTO(BorrowRecord r) {
        boolean returned = BorrowRecord.RETURNED.equals(r.getStatus());
        LocalDate asOf = returned && r.getReturnDate() != null
                ? r.getReturnDate().toLocalDate() : LocalDate.now();
        int overdueDays = FineCalculator.overdueDays(r.getDueDate(), asOf);
        return new BorrowRecordDTO(
                r.getId(), r.getReaderId(), r.getBookId(), r.getBookTitle(),
                r.getBorrowDate(), r.getDueDate(), r.getReturnDate(), r.getStatus(),
                !returned && overdueDays > 0,
                overdueDays,
                FineCalculator.fineAmount(overdueDays, finePerDay)
        );
    }
}
