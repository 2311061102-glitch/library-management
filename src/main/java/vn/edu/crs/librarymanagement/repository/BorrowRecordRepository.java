package vn.edu.crs.librarymanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.crs.librarymanagement.entity.BorrowRecord;

import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByUserId(Long userId);
    List<BorrowRecord> findByUserIdAndStatus(Long userId, String status);
    long countByUserIdAndStatus(Long userId, String status);
    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, String status);
}