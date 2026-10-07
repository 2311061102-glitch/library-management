package vn.edu.library.borrowservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.library.borrowservice.entity.Fine;

import java.util.List;

public interface FineRepository extends JpaRepository<Fine, Long> {

    List<Fine> findByReaderIdOrderByIdDesc(Long readerId);

    Page<Fine> findByPaid(Boolean paid, Pageable pageable);

    boolean existsByReaderIdAndPaidFalse(Long readerId);

    long countByReaderIdAndPaidFalse(Long readerId);

    @Query("select coalesce(sum(f.amount), 0) from Fine f where f.readerId = :readerId and f.paid = false")
    long sumAmountByReaderIdAndPaidFalse(@Param("readerId") Long readerId);

    long countByReaderId(Long readerId);

    long countByReaderIdAndPaidTrue(Long readerId);

    @Query("select coalesce(sum(f.amount), 0) from Fine f where f.readerId = :readerId and f.paid = true")
    long sumAmountByReaderIdAndPaidTrue(@Param("readerId") Long readerId);

    long countByPaidFalse();

    @Query("select coalesce(sum(f.amount), 0) from Fine f where f.paid = false")
    long sumAmountByPaidFalse();

    java.util.Optional<Fine> findByBorrowRecordId(Long borrowRecordId);
}
