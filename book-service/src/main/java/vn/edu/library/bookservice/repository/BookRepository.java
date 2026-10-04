package vn.edu.library.bookservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.library.bookservice.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbnIgnoreCase(String isbn);

    boolean existsByIsbnIgnoreCaseAndIdNot(String isbn, Long id);

    long countByCategoryId(Long categoryId);

    /**
     * Tìm theo từ khóa (tên sách hoặc tác giả) và lọc tùy chọn theo thể loại, có phân trang.
     * keyword truyền vào là chuỗi rỗng khi không tìm kiếm (LIKE '%%' khớp tất cả).
     */
    @Query("""
            SELECT b FROM Book b
            WHERE (:categoryId IS NULL OR b.category.id = :categoryId)
              AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Book> search(@Param("keyword") String keyword,
                      @Param("categoryId") Long categoryId,
                      Pageable pageable);
}
