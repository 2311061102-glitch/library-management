package vn.edu.crs.librarymanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.librarymanagement.entity.BorrowRecord;
import vn.edu.crs.librarymanagement.service.BorrowService;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/borrows")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping("/eligibility")
    public ResponseEntity<?> checkEligibility(@RequestParam Long userId, @RequestParam Long bookId) {
        try {
            return ResponseEntity.ok(borrowService.checkBorrowEligibility(userId, bookId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===== Tạo phiếu mượn mới =====
    // Body: {"userId": 2, "bookId": 1}
    @PostMapping
    public ResponseEntity<?> borrowBook(@RequestBody BorrowRequest request) {
        try {
            BorrowRecord record = borrowService.borrowBook(request.getUserId(), request.getBookId());
            return ResponseEntity.status(HttpStatus.CREATED).body(record);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===== Gia hạn phiếu mượn =====
    @PutMapping("/{id}/renew")
    public ResponseEntity<?> renewBorrow(@PathVariable Long id,
                                         @RequestParam(required = false) Long userId) {
        try {
            BorrowRecord record = borrowService.renewBorrow(id, userId);
            return ResponseEntity.ok(record);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===== Trả sách =====
    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long id,
                                        @RequestParam(required = false) Long userId) {
        try {
            BorrowRecord record = borrowService.returnBook(id, userId);
            return ResponseEntity.ok(record);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/return-details")
    public ResponseEntity<?> returnBookWithCondition(@PathVariable Long id,
                                                      @RequestParam Long userId,
                                                      @RequestBody ReturnRequest request) {
        try {
            return ResponseEntity.ok(borrowService.returnBook(id, userId, request.getCondition(), request.getNote()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> reserveBook(@RequestBody BorrowRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(borrowService.reserveBook(request.getUserId(), request.getBookId()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/return-by-barcode")
    public ResponseEntity<?> returnByBarcode(@RequestBody BarcodeReturnRequest request) {
        try {
            return ResponseEntity.ok(borrowService.returnByBarcode(
                    request.getBarcode(), request.getCondition(), request.getNote()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // ===== Xem lịch sử mượn của 1 người dùng =====
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BorrowRecord>> getBorrowsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(borrowService.getBorrowsByUser(userId));
    }

    // ===== Xem tất cả phiếu mượn (dành cho ADMIN) =====
    @GetMapping
    public ResponseEntity<?> getAllBorrows(@RequestParam String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ban khong co quyen xem toan bo phieu muon");
        }
        return ResponseEntity.ok(borrowService.getAllBorrows());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowRecord> getBorrowById(@PathVariable Long id) {
        return borrowService.getBorrowById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // ===== DTO nội bộ cho request tạo phiếu mượn =====
    public static class BorrowRequest {
        private Long userId;
        private Long bookId;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }
    }

    public static class ReturnRequest {
        private String condition;
        private String note;
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    public static class BarcodeReturnRequest {
        private String barcode;
        private String condition;
        private String note;
        public String getBarcode() { return barcode; }
        public void setBarcode(String barcode) { this.barcode = barcode; }
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }
}