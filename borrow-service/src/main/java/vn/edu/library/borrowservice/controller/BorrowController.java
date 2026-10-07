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
import vn.edu.library.borrowservice.dto.BorrowAdminSummaryDTO;
import vn.edu.library.borrowservice.dto.ReturnBookRequestDTO;
import vn.edu.library.borrowservice.service.BorrowService;
import vn.edu.library.borrowservice.service.AuditService;

import java.util.List;

@RestController
@RequestMapping("/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;
    private final AuditService auditService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowRecordDTO borrow(@Valid @RequestBody BorrowRequestDTO dto, Authentication authentication) {
        BorrowRecordDTO result = borrowService.borrow(dto, userId(authentication), isLibrarian(authentication));
        auditService.record(userId(authentication), "BORROW", "BORROW_RECORD", result.getId(),
                "bookId=" + result.getBookId());
        return result;
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

    @GetMapping("/admin/summary")
    public BorrowAdminSummaryDTO getAdminSummary() {
        return borrowService.getAdminSummary();
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
        BorrowRecordDTO result = borrowService.returnBook(id, userId(authentication), isLibrarian(authentication));
        auditService.record(userId(authentication), "RETURN", "BORROW_RECORD", id, "condition=GOOD");
        return result;
    }

    @PutMapping("/{id}/return/details")
    public BorrowRecordDTO returnBookWithDetails(@PathVariable Long id,
                                                  @Valid @RequestBody ReturnBookRequestDTO request,
                                                  Authentication authentication) {
        BorrowRecordDTO result = borrowService.returnBook(id, request, userId(authentication), isLibrarian(authentication));
        auditService.record(userId(authentication), "RETURN", "BORROW_RECORD", id,
                "condition=" + request.getCondition());
        return result;
    }

    @GetMapping("/{id}")
    public BorrowRecordDTO getById(@PathVariable Long id, Authentication authentication) {
        return borrowService.getById(id, userId(authentication), isLibrarian(authentication));
    }

    @PatchMapping("/{id}/renew")
    public BorrowRecordDTO renew(@PathVariable Long id, Authentication authentication) {
        BorrowRecordDTO result = borrowService.renew(id, userId(authentication), isLibrarian(authentication));
        auditService.record(userId(authentication), "RENEW", "BORROW_RECORD", id, null);
        return result;
    }

    static Long userId(Authentication authentication) {
        return (Long) authentication.getCredentials();
    }

    static boolean isLibrarian(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_LIBRARIAN".equals(a.getAuthority()));
    }
}
