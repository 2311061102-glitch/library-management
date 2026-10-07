package vn.edu.library.borrowservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.library.borrowservice.dto.FineDTO;
import vn.edu.library.borrowservice.dto.FineSummaryDTO;
import vn.edu.library.borrowservice.entity.Fine;
import vn.edu.library.borrowservice.repository.FineRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FineService {

    private final FineRepository fineRepository;

    public List<FineDTO> getMyFines(Long readerId) {
        return fineRepository.findByReaderIdOrderByIdDesc(readerId).stream().map(this::toDTO).toList();
    }

    public FineSummaryDTO getMySummary(Long readerId) {
        long total = fineRepository.countByReaderId(readerId);
        long unpaid = fineRepository.countByReaderIdAndPaidFalse(readerId);
        long paid = fineRepository.countByReaderIdAndPaidTrue(readerId);
        return new FineSummaryDTO(
                total,
                unpaid,
                fineRepository.sumAmountByReaderIdAndPaidFalse(readerId),
                paid,
                fineRepository.sumAmountByReaderIdAndPaidTrue(readerId)
        );
    }

    /** Dành cho thủ thư: xem toàn bộ khoản phạt, lọc tùy chọn theo trạng thái thanh toán. */
    public Page<FineDTO> getAll(Boolean paid, Pageable pageable) {
        Page<Fine> page = (paid == null) ? fineRepository.findAll(pageable) : fineRepository.findByPaid(paid, pageable);
        return page.map(this::toDTO);
    }

    /** Thủ thư xác nhận độc giả đã nộp phạt. */
    public FineDTO pay(Long fineId) {
        if (fineId == null || fineId <= 0) {
            throw new IllegalArgumentException("id khoản phạt phải là số dương");
        }
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy khoản phạt id = " + fineId));
        if (Boolean.TRUE.equals(fine.getPaid())) {
            throw new IllegalStateException("Khoản phạt này đã được thanh toán");
        }
        fine.setPaid(true);
        fine.setPaidAt(LocalDateTime.now());
        return toDTO(fineRepository.save(fine));
    }

    private FineDTO toDTO(Fine f) {
        return new FineDTO(
                f.getId(), f.getBorrowRecord().getId(), f.getReaderId(), f.getBorrowRecord().getBookTitle(),
                f.getOverdueDays(), f.getAmount(), f.getReason(), f.getPaid(), f.getCreatedAt(), f.getPaidAt()
        );
    }
}
