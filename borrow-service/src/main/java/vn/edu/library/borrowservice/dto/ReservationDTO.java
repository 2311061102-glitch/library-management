package vn.edu.library.borrowservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReservationDTO {
    private Long id;
    private Long readerId;
    private Long bookId;
    private String bookTitle;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
