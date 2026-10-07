package vn.edu.library.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApiKeyCreateRequestDTO {
    @NotBlank(message = "Tên đối tác không được để trống")
    private String ownerName;

    @NotBlank(message = "Danh sách scope không được để trống")
    private String scopes; // ví dụ: "books:read"

    private Integer validDays; // số ngày hiệu lực; null = không giới hạn
}