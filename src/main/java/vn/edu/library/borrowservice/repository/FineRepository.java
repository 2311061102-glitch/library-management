package vn.edu.library.borrowservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.library.borrowservice.entity.Fine;

import java.util.List;

public interface FineRepository extends JpaRepository<Fine, Long> {

    List<Fine> findByReaderIdOrderByIdDesc(Long readerId);

    Page<Fine> findByPaid(Boolean paid, Pageable pageable);

    boolean existsByReaderIdAndPaidFalse(Long readerId);
}
