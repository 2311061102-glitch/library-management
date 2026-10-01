package vn.edu.crs.librarymanagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.crs.librarymanagement.entity.Book;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByCategoryId(Long categoryId);

    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Book> findByCategoryIdAndTitleContainingIgnoreCase(Long categoryId, String title, Pageable pageable);
}