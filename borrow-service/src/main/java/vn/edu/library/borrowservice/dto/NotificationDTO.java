package vn.edu.library.borrowservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private Boolean read;
    private LocalDateTime createdAt;
}
