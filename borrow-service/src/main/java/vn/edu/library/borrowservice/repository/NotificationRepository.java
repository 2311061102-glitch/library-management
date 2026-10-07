package vn.edu.library.borrowservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.library.borrowservice.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReaderIdOrderByIdDesc(Long readerId);
    long countByReaderIdAndReadFalse(Long readerId);
}
