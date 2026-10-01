package vn.edu.crs.librarymanagement.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import vn.edu.crs.librarymanagement.entity.Book;
import vn.edu.crs.librarymanagement.entity.Category;
import vn.edu.crs.librarymanagement.repository.BookRepository;
import vn.edu.crs.librarymanagement.repository.CategoryRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository,
                       FileStorageService fileStorageService) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    // ===== Tìm kiếm + Phân trang + Sắp xếp =====
    public Page<Book> searchBooks(String title, Long categoryId, Pageable pageable) {
        boolean hasTitle = title != null && !title.isEmpty();
        boolean hasCategory = categoryId != null;

        if (hasTitle && hasCategory) {
            return bookRepository.findByCategoryIdAndTitleContainingIgnoreCase(categoryId, title, pageable);
        } else if (hasTitle) {
            return bookRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else if (hasCategory) {
            return bookRepository.findByCategoryId(categoryId, pageable);
        } else {
            return bookRepository.findAll(pageable);
        }
    }

    public Book createBook(String role, Book book) {
        checkAdminRole(role);
        return bookRepository.save(book);
    }

    public Optional<Book> updateBook(String role, Long id, Book updatedBook) {
        checkAdminRole(role);
        return bookRepository.findById(id).map(book -> {
            book.setTitle(updatedBook.getTitle());
            book.setAuthor(updatedBook.getAuthor());
            book.setDescription(updatedBook.getDescription());
            book.setImageUrl(updatedBook.getImageUrl());
            book.setTotalCopies(updatedBook.getTotalCopies());
            book.setAvailableCopies(updatedBook.getAvailableCopies());
            book.setCategory(updatedBook.getCategory());
            return bookRepository.save(book);
        });
    }

    public boolean deleteBook(String role, Long id) {
        checkAdminRole(role);
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Book> getBooksByCategory(Long categoryId) {
        return bookRepository.findByCategoryId(categoryId);
    }

    public Optional<Book> createBookInCategory(String role, Long categoryId, Book book) {
        checkAdminRole(role);
        return categoryRepository.findById(categoryId).map(category -> {
            book.setCategory(category);
            return bookRepository.save(book);
        });
    }

    public Optional<Book> updateBookImage(String role, Long id, MultipartFile file) throws IOException {
        checkAdminRole(role);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui long chon file anh hop le");
        }

        return bookRepository.findById(id).map(book -> {
            try {
                String imagePath = fileStorageService.saveFile(file);
                book.setImageUrl(imagePath);
                return bookRepository.save(book);
            } catch (IOException e) {
                throw new RuntimeException("Loi luu file: " + e.getMessage());
            }
        });
    }

    private void checkAdminRole(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new SecurityException("Ban khong co quyen thuc hien thao tac nay");
        }
    }
}