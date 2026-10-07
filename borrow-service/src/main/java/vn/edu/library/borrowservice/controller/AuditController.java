package vn.edu.library.borrowservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.library.borrowservice.dto.AuditLogDTO;
import vn.edu.library.borrowservice.service.AuditService;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AuditController {
    private final AuditService service;

    @GetMapping
    public Page<AuditLogDTO> getAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.getAll(pageable);
    }
}
