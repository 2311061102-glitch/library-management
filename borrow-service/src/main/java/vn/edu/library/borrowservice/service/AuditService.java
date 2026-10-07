package vn.edu.library.borrowservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.library.borrowservice.dto.AuditLogDTO;
import vn.edu.library.borrowservice.entity.AuditLog;
import vn.edu.library.borrowservice.repository.AuditLogRepository;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository repository;

    public void record(Long actorId, String action, String targetType, Long targetId, String detail) {
        AuditLog log = new AuditLog();
        log.setActorId(actorId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setCreatedAt(java.time.LocalDateTime.now());
        repository.save(log);
    }

    public Page<AuditLogDTO> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }

    private AuditLogDTO toDTO(AuditLog l) {
        return new AuditLogDTO(l.getId(), l.getActorId(), l.getAction(), l.getTargetType(),
                l.getTargetId(), l.getDetail(), l.getCreatedAt());
    }
}
