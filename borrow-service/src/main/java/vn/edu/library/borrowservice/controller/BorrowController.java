package vn.edu.library.borrowservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.library.borrowservice.dto.BorrowRecordDTO;
import vn.edu.library.borrowservice.dto.BorrowRequestDTO;
import vn.edu.library.borrowservice.dto.BorrowSummaryDTO;
import vn.edu.library.borrowservice.service.BorrowService;

import java.util.List;

@RestController
@RequestMapping("/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowRecordDTO borrow(@Valid @RequestBody BorrowRequestDTO dto, Authentication authentication) {
        return borrowService.borrow(dto, userId(authentication), isLibrarian(authentication));
    }

    @GetMapping("/my")
    public List<BorrowRecordDTO> getMyBorrows(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean overdue,
            Authentication authentication) {
        return borrowService.getMyBorrows(userId(authentication), status, overdue);
    }

    @GetMapping("/my/summary")
    public BorrowSummaryDTO getMySummary(Authentication authentication) {
        return borrowService.getMySummary(userId(authentication));
    }

    // Chỉ thủ thư (đã chặn ở SecurityConfig): GET /borrows?status=BORROWING&page=0&size=10
    @GetMapping
    public Page<BorrowRecordDTO> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long readerId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return borrowService.getAll(status, readerId, pageable);
    }

    @PutMapping("/{id}/return")
    public BorrowRecordDTO returnBook(@PathVariable Long id, Authentication authentication) {
        return borrowService.returnBook(id, userId(authentication), isLibrarian(authentication));
    }

    @PatchMapping("/{id}/renew")
    public BorrowRecordDTO renew(@PathVariable Long id, Authentication authentication) {
        return borrowService.renew(id, userId(authentication), isLibrarian(authentication));
    }

    static Long userId(Authentication authentication) {
        return (Long) authentication.getCredentials();
    }

    static boolean isLibrarian(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_LIBRARIAN".equals(a.getAuthority()));
    }
}
