package vn.edu.library.borrowservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.library.borrowservice.dto.NotificationDTO;
import vn.edu.library.borrowservice.entity.Notification;
import vn.edu.library.borrowservice.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;

    public void create(Long readerId, String title, String message) {
        Notification n = new Notification();
        n.setReaderId(readerId);
        n.setTitle(title);
        n.setMessage(message);
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());
        repository.save(n);
    }

    public List<NotificationDTO> getMine(Long readerId) {
        return repository.findByReaderIdOrderByIdDesc(readerId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public NotificationDTO markRead(Long id, Long readerId) {
        Notification n = repository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Không tìm thấy thông báo"));
        if (!n.getReaderId().equals(readerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Không có quyền đọc thông báo này");
        }
        n.setRead(true);
        return toDTO(repository.save(n));
    }

    public long unreadCount(Long readerId) {
        return repository.countByReaderIdAndReadFalse(readerId);
    }

    private NotificationDTO toDTO(Notification n) {
        return new NotificationDTO(n.getId(), n.getTitle(), n.getMessage(), n.getRead(), n.getCreatedAt());
    }
}
