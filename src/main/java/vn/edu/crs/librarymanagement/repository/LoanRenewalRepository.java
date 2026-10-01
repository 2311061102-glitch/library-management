package vn.edu.crs.librarymanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.crs.librarymanagement.entity.LoanRenewal;

import java.util.List;

public interface LoanRenewalRepository extends JpaRepository<LoanRenewal, Long> {
    List<LoanRenewal> findByBorrowRecordIdOrderByRenewedAtAsc(Long borrowRecordId);
}
