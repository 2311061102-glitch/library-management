package vn.edu.library.borrowservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReservationRequestDTO {
    @NotNull @Positive
    private Long bookId;
    @Size(max = 255)
    private String bookTitle;
}
