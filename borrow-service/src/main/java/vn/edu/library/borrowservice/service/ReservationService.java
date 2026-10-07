package vn.edu.library.borrowservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.library.borrowservice.dto.ReservationDTO;
import vn.edu.library.borrowservice.dto.ReservationRequestDTO;
import vn.edu.library.borrowservice.entity.Reservation;
import vn.edu.library.borrowservice.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository repository;
    private final NotificationService notificationService;

    public ReservationDTO reserve(ReservationRequestDTO request, Long readerId) {
        if (repository.findFirstByReaderIdAndBookIdAndStatus(readerId, request.getBookId(), Reservation.WAITING).isPresent()) {
            throw new IllegalStateException("Bạn đã có yêu cầu chờ cho sách này");
        }
        Reservation r = new Reservation();
        r.setReaderId(readerId);
        r.setBookId(request.getBookId());
        r.setBookTitle(request.getBookTitle() == null || request.getBookTitle().isBlank()
                ? "Sách #" + request.getBookId() : request.getBookTitle().trim());
        r.setStatus(Reservation.WAITING);
        r.setCreatedAt(LocalDateTime.now());
        return toDTO(repository.save(r));
    }

    public List<ReservationDTO> getMine(Long readerId) {
        return repository.findByReaderIdOrderByIdDesc(readerId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public ReservationDTO cancel(Long id, Long readerId, boolean librarian) {
        Reservation r = find(id);
        if (!librarian && !r.getReaderId().equals(readerId)) {
            throw new AccessDeniedException("Bạn không có quyền hủy yêu cầu này");
        }
        if (!Reservation.WAITING.equals(r.getStatus())) {
            throw new IllegalStateException("Chỉ có thể hủy yêu cầu đang chờ");
        }
        r.setStatus(Reservation.CANCELLED);
        return toDTO(repository.save(r));
    }

    @Transactional
    public ReservationDTO markReady(Long id) {
        Reservation r = find(id);
        if (!Reservation.WAITING.equals(r.getStatus())) {
            throw new IllegalStateException("Yêu cầu không còn ở trạng thái chờ");
        }
        r.setStatus(Reservation.READY);
        r.setExpiresAt(LocalDateTime.now().plusDays(2));
        notificationService.create(r.getReaderId(), "Sách đã sẵn sàng",
                "Sách " + r.getBookTitle() + " đã sẵn sàng tại thư viện trong 2 ngày.");
        return toDTO(repository.save(r));
    }

    private Reservation find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy yêu cầu đặt trước"));
    }

    private ReservationDTO toDTO(Reservation r) {
        return new ReservationDTO(r.getId(), r.getReaderId(), r.getBookId(), r.getBookTitle(),
                r.getStatus(), r.getCreatedAt(), r.getExpiresAt());
    }
}
