package vn.edu.library.bookservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.edu.library.bookservice.dto.BookDTO;
import vn.edu.library.bookservice.service.BookService;

/** API nội bộ: chỉ borrow-service gọi, KHÔNG được route ra ngoài qua api-gateway. */
@RestController
@RequestMapping("/internal/books")
@RequiredArgsConstructor
public class InternalBookController {

    private final BookService bookService;

    @PatchMapping("/{id}/reserve-copy")
    public BookDTO reserveCopy(@PathVariable Long id) {
        return bookService.reserveCopy(id);
    }

    @PatchMapping("/{id}/release-copy")
    public BookDTO releaseCopy(@PathVariable Long id) {
        return bookService.releaseCopy(id);
    }
}
