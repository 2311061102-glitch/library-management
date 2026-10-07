package vn.edu.library.borrowservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Phiếu mượn sách (thay cho Registration của CRS). */
@Entity
@Table(name = "borrow_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecord {

    public static final String BORROWING = "BORROWING"; // đang mượn
    public static final String RETURNED = "RETURNED";   // đã trả

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // userId của độc giả (lấy từ JWT do auth-service cấp), không có khóa ngoại vì User nằm ở database khác
    @Column(name = "reader_id", nullable = false)
    private Long readerId;

    // Chỉ lưu dạng số, KHÔNG dùng @ManyToOne tới Book vì Book nằm ở database khác
    @Column(name = "book_id", nullable = false)
    private Long bookId;

    // Lưu kèm tên sách tại thời điểm mượn để hiển thị lịch sử mà không phải gọi sang book-service
    @Column(name = "book_title", nullable = false, length = 255)
    private String bookTitle;

    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(nullable = false, length = 20)
    private String status; // BORROWING / RETURNED
}
