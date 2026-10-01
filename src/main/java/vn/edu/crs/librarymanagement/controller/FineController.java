package vn.edu.crs.librarymanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.librarymanagement.entity.Fine;
import vn.edu.crs.librarymanagement.service.FineService;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/fines")
public class FineController {

    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    // ===== Xem phiếu phạt của 1 người dùng =====
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Fine>> getFinesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(fineService.getFinesByUser(userId));
    }

    // ===== Xem tất cả phiếu phạt (ADMIN) =====
    @GetMapping
    public ResponseEntity<?> getAllFines(@RequestParam String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ban khong co quyen xem toan bo phieu phat");
        }
        return ResponseEntity.ok(fineService.getAllFines());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fine> getFineById(@PathVariable Long id) {
        return fineService.getFineById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // ===== Đánh dấu đã thanh toán (chỉ ADMIN) =====
    @PutMapping("/{id}/pay")
    public ResponseEntity<?> markAsPaid(@RequestParam String role, @PathVariable Long id) {
        try {
            Fine fine = fineService.markAsPaid(role, id);
            return ResponseEntity.ok(fine);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}