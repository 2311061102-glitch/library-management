package vn.edu.library.borrowservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_reservation", uniqueConstraints = @UniqueConstraint(
        name = "uk_reservation_reader_book_waiting", columnNames = {"reader_id", "book_id", "status"}))
@Data
@NoArgsConstructor
public class Reservation {
    public static final String WAITING = "WAITING";
    public static final String READY = "READY";
    public static final String CANCELLED = "CANCELLED";
    public static final String EXPIRED = "EXPIRED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "reader_id", nullable = false)
    private Long readerId;
    @Column(name = "book_id", nullable = false)
    private Long bookId;
    @Column(name = "book_title", nullable = false)
    private String bookTitle;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
