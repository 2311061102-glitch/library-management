package vn.edu.crs.librarymanagement.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.crs.librarymanagement.entity.BookCopy;

import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from BookCopy c where c.book.id = :bookId and c.status = 'AVAILABLE' order by c.id")
    List<BookCopy> findAvailableForUpdate(@Param("bookId") Long bookId);

    Optional<BookCopy> findByBarcode(String barcode);
    long countByBookIdAndStatus(Long bookId, String status);
}
