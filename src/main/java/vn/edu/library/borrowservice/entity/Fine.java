package vn.edu.library.borrowservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Khoản phạt do trả sách trễ hạn. Cùng database với BorrowRecord nên dùng được khóa ngoại thật. */
@Entity
@Table(name = "fine")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "borrow_record_id", nullable = false, unique = true)
    private BorrowRecord borrowRecord;

    @Column(name = "reader_id", nullable = false)
    private Long readerId;

    @Column(name = "overdue_days", nullable = false)
    private Integer overdueDays;

    @Column(nullable = false)
    private Long amount; // VND

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(nullable = false)
    private Boolean paid;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
