package com.example.bookexchange.controller;

import com.example.bookexchange.entity.Book;
import com.example.bookexchange.entity.BookListingType;
import com.example.bookexchange.service.BookService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private static final Set<String> BOOK_CATEGORIES = Set.of(
            "Giáo khoa", "Tiểu thuyết", "Kinh tế", "Kỹ thuật",
            "Tâm lý", "Văn học", "Truyện tranh", "Khác");

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> getBooks(@RequestParam(required = false) String q) {
        return bookService.searchBooks(q);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book createBookListing(@RequestBody Book book) {
        if (!StringUtils.hasText(book.getTitle()) || !StringUtils.hasText(book.getAuthor())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Tên sách và tác giả không được để trống.");
        }
        if (!BOOK_CATEGORIES.contains(book.getCategory())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Vui lòng chọn danh mục sách hợp lệ.");
        }
        if (book.getListingType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Vui lòng chọn bán sách hoặc trao đổi sách.");
        }
        if (book.getListingType() == BookListingType.BAN
                && book.getPrice() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Vui lòng nhập giá bán cho sách.");
        }
        return bookService.saveBook(book);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleListingError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(Map.of("message", exception.getReason()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableListing() {
        return ResponseEntity.badRequest()
                .body(Map.of("message", "Dữ liệu đăng sách không hợp lệ. Vui lòng kiểm tra loại tin đăng và thông tin sách."));
    }
}
