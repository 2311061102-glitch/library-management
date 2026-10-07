package vn.edu.library.borrowservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import vn.edu.library.borrowservice.dto.NotificationDTO;
import vn.edu.library.borrowservice.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    @GetMapping("/my")
    public List<NotificationDTO> mine(Authentication auth) {
        return service.getMine(BorrowController.userId(auth));
    }

    @GetMapping("/my/unread-count")
    public long unread(Authentication auth) {
        return service.unreadCount(BorrowController.userId(auth));
    }

    @PatchMapping("/{id}/read")
    public NotificationDTO read(@PathVariable Long id, Authentication auth) {
        return service.markRead(id, BorrowController.userId(auth));
    }
}
