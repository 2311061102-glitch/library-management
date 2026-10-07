package vn.edu.library.borrowservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private Long actorId;
    private String action;
    private String targetType;
    private Long targetId;
    private String detail;
    private LocalDateTime createdAt;
}
