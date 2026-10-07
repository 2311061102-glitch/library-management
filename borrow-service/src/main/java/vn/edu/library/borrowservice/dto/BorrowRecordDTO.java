package vn.edu.library.borrowservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BorrowRecordDTO {
    private Long id;
    private Long readerId;
    private Long bookId;
    private String bookTitle;
    private LocalDateTime borrowDate;
    private LocalDate dueDate;
    private LocalDateTime returnDate;
    private String status;
    private boolean overdue;      // đang mượn và đã quá hạn
    private int overdueDays;      // số ngày quá hạn tính đến hiện tại (hoặc đến lúc trả)
    private long estimatedFine;   // tiền phạt dự kiến (VND)
    private int renewalCount;
    private int remainingRenewals;
}
