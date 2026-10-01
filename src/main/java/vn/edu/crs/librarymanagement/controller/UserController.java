package vn.edu.crs.librarymanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.librarymanagement.entity.User;
import vn.edu.crs.librarymanagement.service.UserService;
import java.util.Optional;
import java.util.List;
import vn.edu.crs.librarymanagement.entity.BorrowRecord;
import vn.edu.crs.librarymanagement.entity.Fine;
import vn.edu.crs.librarymanagement.service.BorrowService;
import vn.edu.crs.librarymanagement.service.FineService;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final BorrowService borrowService;
    private final FineService fineService;

    public UserController(UserService userService, BorrowService borrowService, FineService fineService) {
        this.userService = userService;
        this.borrowService = borrowService;
        this.fineService = fineService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id) ?
                new ResponseEntity<>(HttpStatus.NO_CONTENT) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        Optional<User> userOpt = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tai khoan hoac mat khau");
        }
    }
    // ===== Chi tiết độc giả: thông tin + lịch sử mượn + phiếu phạt (dành cho ADMIN) =====
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getUserDetails(@RequestParam String role, @PathVariable Long id) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ban khong co quyen xem thong tin nay");
        }

        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Khong tim thay nguoi dung");
        }

        List<BorrowRecord> borrowHistory = borrowService.getBorrowsByUser(id);
        long currentlyBorrowing = borrowHistory.stream()
                .filter(r -> "BORROWING".equals(r.getStatus()))
                .count();
        List<Fine> fines = fineService.getFinesByUser(id);
        boolean hasUnpaidFine = fines.stream().anyMatch(f -> "UNPAID".equals(f.getStatus()));

        Map<String, Object> result = new HashMap<>();
        result.put("user", userOpt.get());
        result.put("borrowHistory", borrowHistory);
        result.put("currentlyBorrowing", currentlyBorrowing);
        result.put("fines", fines);
        result.put("hasUnpaidFine", hasUnpaidFine);

        return ResponseEntity.ok(result);
    }
    // ===== Dang ky tai khoan moi (cong khai, luon la CUSTOMER) =====
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User newUser) {
        // Kiem tra username da ton tai chua
        if (userService.getByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ten dang nhap da ton tai");
        }
        newUser.setRole(User.Role.CUSTOMER); // ep cung, khong cho tu chon ADMIN
        User saved = userService.createUser(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    // ===== Doi mat khau (yeu cau nhap dung mat khau cu) =====
    @PutMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String oldPassword = body.get("oldPassword");
            String newPassword = body.get("newPassword");
            userService.changePassword(id, oldPassword, newPassword);
            return ResponseEntity.ok("Doi mat khau thanh cong");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
