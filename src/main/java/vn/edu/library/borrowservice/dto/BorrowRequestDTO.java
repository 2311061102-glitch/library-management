package vn.edu.library.borrowservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowRequestDTO {

    @NotNull(message = "bookId không được để trống")
    private Long bookId;

    // Chỉ thủ thư mới dùng (mượn hộ độc giả). Với độc giả, server luôn lấy userId từ JWT, bỏ qua trường này.
    private Long readerId;
}
