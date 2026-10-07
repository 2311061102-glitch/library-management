package com.example.bookexchange.service;

import com.example.bookexchange.entity.Book;
import com.example.bookexchange.repository.BookRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public List<Book> searchBooks(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getAllBooks();
        }
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword);
    }
}
