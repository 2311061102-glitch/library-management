package vn.edu.crs.librarymanagement.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.crs.librarymanagement.entity.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByBookIdAndUserIdAndStatus(Long bookId, Long userId, String status);
    List<Reservation> findByBookIdAndStatusOrderByReservedAtAsc(Long bookId, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.book.id = :bookId and r.status = 'WAITING' order by r.reservedAt")
    List<Reservation> findWaitingForUpdate(@Param("bookId") Long bookId);
}
