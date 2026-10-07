package vn.edu.library.borrowservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.library.borrowservice.entity.BorrowRecord;
import vn.edu.library.borrowservice.entity.Fine;
import vn.edu.library.borrowservice.entity.Reservation;
import vn.edu.library.borrowservice.repository.BorrowRecordRepository;
import vn.edu.library.borrowservice.repository.FineRepository;
import vn.edu.library.borrowservice.repository.ReservationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BorrowMaintenanceService {
    private final BorrowRecordRepository borrowRepository;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    @Value("${borrow.fine-per-day:5000}")
    private long finePerDay;

    @Scheduled(cron = "${borrow.maintenance-cron:0 0 2 * * *}")
    @Transactional
    public void maintain() {
        LocalDate today = LocalDate.now();
        borrowRepository.findAll().stream()
                .filter(r -> BorrowRecord.BORROWING.equals(r.getStatus()))
                .forEach(record -> {
                    int overdue = Math.max(0, (int) (today.toEpochDay() - record.getDueDate().toEpochDay()));
                    if (overdue > 0) {
                        Fine fine = fineRepository.findByBorrowRecordId(record.getId()).orElseGet(Fine::new);
                        fine.setBorrowRecord(record);
                        fine.setReaderId(record.getReaderId());
                        fine.setOverdueDays(overdue);
                        fine.setAmount(overdue * finePerDay);
                        fine.setReason("Đang quá hạn " + overdue + " ngày");
                        if (fine.getPaid() == null) fine.setPaid(false);
                        if (fine.getCreatedAt() == null) fine.setCreatedAt(LocalDateTime.now());
                        fineRepository.save(fine);
                        notificationService.create(record.getReaderId(), "Sách quá hạn",
                                "Sách " + record.getBookTitle() + " đã quá hạn " + overdue + " ngày.");
                    } else if (record.getDueDate().minusDays(3).equals(today)) {
                        notificationService.create(record.getReaderId(), "Sắp đến hạn trả",
                                "Sách " + record.getBookTitle() + " còn 3 ngày trước hạn trả.");
                    }
                });
        reservationRepository.findByStatusOrderByCreatedAtAsc(Reservation.READY).stream()
                .filter(r -> r.getExpiresAt() != null && r.getExpiresAt().isBefore(LocalDateTime.now()))
                .forEach(r -> { r.setStatus(Reservation.EXPIRED); reservationRepository.save(r); });
    }
}
