package vn.edu.library.borrowservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_notification")
@Data
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "reader_id", nullable = false)
    private Long readerId;
    @Column(nullable = false, length = 120)
    private String title;
    @Column(nullable = false, length = 500)
    private String message;
    @Column(nullable = false)
    private Boolean read = false;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
