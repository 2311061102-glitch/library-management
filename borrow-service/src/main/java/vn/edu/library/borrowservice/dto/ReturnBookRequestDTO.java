package vn.edu.library.borrowservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReturnBookRequestDTO {
    @NotBlank(message = "condition không được để trống")
    private String condition = "GOOD";
    private String note;
}
