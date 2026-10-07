package vn.edu.library.borrowservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FineDTO {
    private Long id;
    private Long borrowRecordId;
    private Long readerId;
    private String bookTitle;
    private Integer overdueDays;
    private Long amount;
    private String reason;
    private Boolean paid;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
