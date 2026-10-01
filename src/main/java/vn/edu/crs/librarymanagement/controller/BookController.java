package vn.edu.crs.librarymanagement.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.crs.librarymanagement.entity.Book;
import vn.edu.crs.librarymanagement.entity.Category;
import vn.edu.crs.librarymanagement.service.BookService;
import vn.edu.crs.librarymanagement.service.CategoryService;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;
    private final CategoryService categoryService;

    public BookController(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    // ===== Danh sách sách: tìm kiếm + phân trang + sắp xếp =====
    // Ví dụ: GET /books?title=dac&page=0&size=5&sort=title,asc
    @GetMapping
    public ResponseEntity<Page<Book>> getBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            Pageable pageable
    ) {
        Page<Book> books = bookService.searchBooks(title, categoryId, pageable);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<?> createBook(@RequestParam String role, @RequestBody Book book) {
        try {
            if (book.getCategory() != null && book.getCategory().getId() != null) {
                Optional<Category> categoryOpt = categoryService.getCategoryById(book.getCategory().getId());
                if (categoryOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category khong ton tai");
                }
                book.setCategory(categoryOpt.get());
            }
            Book saved = bookService.createBook(role, book);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@RequestParam String role, @PathVariable Long id, @RequestBody Book book) {
        try {
            if (book.getCategory() != null && book.getCategory().getId() != null) {
                Optional<Category> categoryOpt = categoryService.getCategoryById(book.getCategory().getId());
                if (categoryOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category khong ton tai");
                }
                book.setCategory(categoryOpt.get());
            }
            return bookService.updateBook(role, id, book)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@RequestParam String role, @PathVariable Long id) {
        try {
            boolean deleted = bookService.deleteBook(role, id);
            return deleted
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // ===== Upload / cập nhật ảnh bìa sách (chỉ ADMIN) =====
    @PutMapping("/{id}/image")
    public ResponseEntity<?> updateBookImage(
            @RequestParam String role,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            Optional<Book> updated = bookService.updateBookImage(role, id, file);
            return updated
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Loi luu file: " + e.getMessage());
        }
    }
}