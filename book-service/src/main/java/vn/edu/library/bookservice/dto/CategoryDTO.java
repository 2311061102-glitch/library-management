package vn.edu.library.bookservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;

    @NotBlank(message = "Tên thể loại không được để trống")
    private String name;

    private String description;

    // số sách thuộc thể loại (chỉ dùng khi trả về)
    private Long bookCount;
}
