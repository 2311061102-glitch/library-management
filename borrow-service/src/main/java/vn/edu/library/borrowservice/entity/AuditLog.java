package vn.edu.library.borrowservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_audit_log")
@Data
@NoArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "actor_id", nullable = false)
    private Long actorId;
    @Column(nullable = false, length = 40)
    private String action;
    @Column(name = "target_type", nullable = false, length = 40)
    private String targetType;
    @Column(name = "target_id")
    private Long targetId;
    @Column(length = 1000)
    private String detail;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
