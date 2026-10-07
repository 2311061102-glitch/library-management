package vn.edu.library.borrowservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.library.borrowservice.entity.BorrowRecord;

import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByReaderIdOrderByIdDesc(Long readerId);

    Page<BorrowRecord> findByStatus(String status, Pageable pageable);

    long countByReaderIdAndStatus(Long readerId, String status);

    boolean existsByReaderIdAndBookIdAndStatus(Long readerId, Long bookId, String status);
}
