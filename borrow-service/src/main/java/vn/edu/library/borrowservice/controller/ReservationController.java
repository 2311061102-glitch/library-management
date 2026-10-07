package vn.edu.library.borrowservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.library.borrowservice.dto.ReservationDTO;
import vn.edu.library.borrowservice.dto.ReservationRequestDTO;
import vn.edu.library.borrowservice.service.ReservationService;
import vn.edu.library.borrowservice.service.AuditService;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService service;
    private final AuditService auditService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDTO reserve(@Valid @RequestBody ReservationRequestDTO request, Authentication auth) {
        ReservationDTO result = service.reserve(request, BorrowController.userId(auth));
        auditService.record(BorrowController.userId(auth), "RESERVE", "RESERVATION", result.getId(), null);
        return result;
    }

    @GetMapping("/my")
    public List<ReservationDTO> mine(Authentication auth) {
        return service.getMine(BorrowController.userId(auth));
    }

    @DeleteMapping("/{id}")
    public ReservationDTO cancel(@PathVariable Long id, Authentication auth) {
        ReservationDTO result = service.cancel(id, BorrowController.userId(auth), BorrowController.isLibrarian(auth));
        auditService.record(BorrowController.userId(auth), "CANCEL_RESERVATION", "RESERVATION", id, null);
        return result;
    }

    @PatchMapping("/{id}/ready")
    public ReservationDTO ready(@PathVariable Long id, Authentication auth) {
        ReservationDTO result = service.markReady(id);
        auditService.record(BorrowController.userId(auth), "READY_RESERVATION", "RESERVATION", id, null);
        return result;
    }
}
