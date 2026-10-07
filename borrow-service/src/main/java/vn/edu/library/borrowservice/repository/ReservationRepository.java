package vn.edu.library.borrowservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.library.borrowservice.entity.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByReaderIdOrderByIdDesc(Long readerId);
    List<Reservation> findByStatusOrderByCreatedAtAsc(String status);
    Optional<Reservation> findFirstByReaderIdAndBookIdAndStatus(Long readerId, Long bookId, String status);
}
