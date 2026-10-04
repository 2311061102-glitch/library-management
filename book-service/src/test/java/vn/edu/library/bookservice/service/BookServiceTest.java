package vn.edu.library.bookservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.library.bookservice.dto.BookDTO;
import vn.edu.library.bookservice.entity.Book;
import vn.edu.library.bookservice.entity.Category;
import vn.edu.library.bookservice.repository.BookRepository;
import vn.edu.library.bookservice.repository.CategoryRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private BookService bookService;

    private Book book;

    @BeforeEach
    void setUp() {
        Category category = new Category(1L, "Công nghệ thông tin", null);
        book = new Book(10L, "Clean Code", "Robert C. Martin", "9780132350884", 2008, category, 3, 3);
    }

    @Test
    void reserveCopy_giamSoBanConLaiDi1() {
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        BookDTO result = bookService.reserveCopy(10L);

        assertEquals(2, result.getAvailableCopies());
    }

    @Test
    void reserveCopy_nemLoiKhiHetBan() {
        book.setAvailableCopies(0);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        assertThrows(IllegalStateException.class, () -> bookService.reserveCopy(10L));
    }

    @Test
    void reserveCopy_nemLoiKhiKhongTimThaySach() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> bookService.reserveCopy(99L));
    }

    @Test
    void releaseCopy_khongVuotQuaTongSoBan() {
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book)); // available == total
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        BookDTO result = bookService.releaseCopy(10L);

        assertEquals(3, result.getAvailableCopies());
    }

    @Test
    void releaseCopy_tangSoBanConLaiDi1() {
        book.setAvailableCopies(1);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(2, bookService.releaseCopy(10L).getAvailableCopies());
    }

    @Test
    void update_khongChoGiamTongXuongDuoiSoBanDangMuon() {
        book.setAvailableCopies(1); // đang có 2 bản được mượn
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbnIgnoreCaseAndIdNot("9780132350884", 10L)).thenReturn(false);

        BookDTO dto = new BookDTO(null, "Clean Code", "Robert C. Martin", "9780132350884", 2008, 1L, null, 1, null);

        assertThrows(IllegalArgumentException.class, () -> bookService.update(10L, dto));
    }
}
