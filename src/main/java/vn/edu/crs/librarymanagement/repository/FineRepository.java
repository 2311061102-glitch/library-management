package vn.edu.crs.librarymanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.crs.librarymanagement.entity.Fine;

import java.util.List;

public interface FineRepository extends JpaRepository<Fine, Long> {
    boolean existsByBorrowRecord_User_IdAndStatus(Long userId, String status);
    List<Fine> findByBorrowRecord_User_Id(Long userId);
}