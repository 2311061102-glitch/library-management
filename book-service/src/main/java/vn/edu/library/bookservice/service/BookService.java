package vn.edu.library.bookservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.library.bookservice.dto.BookDTO;
import vn.edu.library.bookservice.entity.Book;
import vn.edu.library.bookservice.entity.Category;
import vn.edu.library.bookservice.repository.BookRepository;
import vn.edu.library.bookservice.repository.CategoryRepository;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookDTO getById(Long id) {
        return toDTO(find(id));
    }

    public BookDTO create(BookDTO dto) {
        if (bookRepository.existsByIsbnIgnoreCase(dto.getIsbn().trim())) {
            throw new IllegalArgumentException("Mã ISBN đã tồn tại");
        }
        Book book = new Book();
        apply(book, dto);
        book.setTotalCopies(dto.getTotalCopies());
        book.setAvailableCopies(dto.getTotalCopies()); // sách mới: toàn bộ bản đều còn trên kệ
        return toDTO(bookRepository.save(book));
    }

    public BookDTO update(Long id, BookDTO dto) {
        Book book = find(id);
        if (bookRepository.existsByIsbnIgnoreCaseAndIdNot(dto.getIsbn().trim(), id)) {
            throw new IllegalArgumentException("Mã ISBN đã tồn tại");
        }

        // Số bản đang được mượn = tổng - còn lại. Tổng mới không được nhỏ hơn số đang cho mượn.
        int borrowed = book.getTotalCopies() - book.getAvailableCopies();
        if (dto.getTotalCopies() < borrowed) {
            throw new IllegalArgumentException(
                    "Tổng số bản không được nhỏ hơn số bản đang được mượn (" + borrowed + ")");
        }

        apply(book, dto);
        book.setTotalCopies(dto.getTotalCopies());
        book.setAvailableCopies(dto.getTotalCopies() - borrowed);
        return toDTO(bookRepository.save(book));
    }

    public void delete(Long id) {
        Book book = find(id);
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            throw new IllegalStateException("Sách đang có bản được mượn, không thể xóa");
        }
        bookRepository.delete(book);
    }

    // ===================== Tìm kiếm + phân trang =====================

    public Page<BookDTO> search(String keyword, Long categoryId, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();
        return bookRepository.search(kw, categoryId, pageable).map(this::toDTO);
    }

    // ===================== API nội bộ reserve/release copy =====================

    @Transactional
    public BookDTO reserveCopy(Long bookId) {
        Book book = find(bookId);
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("Sách đã hết bản, không thể mượn");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        return toDTO(bookRepository.save(book));
    }

    @Transactional
    public BookDTO releaseCopy(Long bookId) {
        Book book = find(bookId);
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
        }
        return toDTO(bookRepository.save(book));
    }

    // ===================== helper =====================

    private Book find(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sách id = " + id));
    }

    private void apply(Book book, BookDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thể loại id = " + dto.getCategoryId()));
        book.setTitle(dto.getTitle().trim());
        book.setAuthor(dto.getAuthor().trim());
        book.setIsbn(dto.getIsbn().trim());
        book.setPublishYear(dto.getPublishYear());
        book.setCategory(category);
    }

    private BookDTO toDTO(Book b) {
        return new BookDTO(
                b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(), b.getPublishYear(),
                b.getCategory().getId(), b.getCategory().getName(),
                b.getTotalCopies(), b.getAvailableCopies()
        );
    }
}
