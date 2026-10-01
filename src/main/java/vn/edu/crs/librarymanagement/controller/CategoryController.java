package vn.edu.crs.librarymanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.librarymanagement.entity.Book;
import vn.edu.crs.librarymanagement.service.BookService;
import vn.edu.crs.librarymanagement.entity.Category;
import vn.edu.crs.librarymanagement.service.CategoryService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final BookService bookService;

    public CategoryController(CategoryService categoryService, BookService bookService) {
        this.categoryService = categoryService;
        this.bookService = bookService;
    }

    // ===== Lấy tất cả Category (ai cũng xem được) =====
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // ===== Lấy Category theo ID =====
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // ===== Tạo Category (chỉ ADMIN) =====
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestParam String role,
                                            @RequestBody Category category) {
        try {
            Category saved = categoryService.createCategory(role, category);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // ===== Cập nhật Category (chỉ ADMIN) =====
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@RequestParam String role,
                                            @PathVariable Long id,
                                            @RequestBody Category category) {
        try {
            Optional<Category> updated = categoryService.updateCategory(role, id, category);
            return updated
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // ===== Xoá Category (chỉ ADMIN) =====
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@RequestParam String role,
                                            @PathVariable Long id) {
        try {
            boolean deleted = categoryService.deleteCategory(role, id);
            return deleted
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // ===== Lấy Book theo Category =====
    @GetMapping("/{id}/books")
    public ResponseEntity<List<Book>> getBooksByCategory(@PathVariable Long id) {
        if (categoryService.getCategoryById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(bookService.getBooksByCategory(id));
    }

    // ===== Thêm Book vào Category (chỉ ADMIN) =====
    @PostMapping("/{id}/books")
    public ResponseEntity<?> createBookInCategory(@RequestParam String role,
                                                  @PathVariable Long id,
                                                  @RequestBody Book book) {
        try {
            Optional<Book> saved = bookService.createBookInCategory(role, id, book);
            return saved
                    .map(b -> ResponseEntity.status(HttpStatus.CREATED).body(b))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}