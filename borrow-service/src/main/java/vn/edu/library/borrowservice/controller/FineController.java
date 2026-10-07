package vn.edu.library.borrowservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.library.borrowservice.dto.FineDTO;
import vn.edu.library.borrowservice.dto.FineSummaryDTO;
import vn.edu.library.borrowservice.service.FineService;
import vn.edu.library.borrowservice.service.AuditService;

import java.util.List;

@RestController
@RequestMapping("/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;
    private final AuditService auditService;

    @GetMapping("/my")
    public List<FineDTO> getMyFines(Authentication authentication) {
        return fineService.getMyFines(BorrowController.userId(authentication));
    }

    @GetMapping("/my/summary")
    public FineSummaryDTO getMySummary(Authentication authentication) {
        return fineService.getMySummary(BorrowController.userId(authentication));
    }

    // Chỉ thủ thư: GET /fines?paid=false&page=0&size=10
    @GetMapping
    public Page<FineDTO> getAll(
            @RequestParam(required = false) Boolean paid,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return fineService.getAll(paid, pageable);
    }

    // Chỉ thủ thư: xác nhận đã thu tiền phạt
    @PatchMapping("/{id}/pay")
    public FineDTO pay(@PathVariable Long id, Authentication authentication) {
        FineDTO result = fineService.pay(id);
        auditService.record(BorrowController.userId(authentication), "PAY_FINE", "FINE", id, null);
        return result;
    }
}
