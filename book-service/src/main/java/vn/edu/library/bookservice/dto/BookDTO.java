package vn.edu.library.bookservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private Long id;

    @NotBlank(message = "Tên sách không được để trống")
    private String title;

    @NotBlank(message = "Tác giả không được để trống")
    private String author;

    @NotBlank(message = "Mã ISBN không được để trống")
    private String isbn;

    private Integer publishYear;

    @NotNull(message = "Thể loại không được để trống")
    private Long categoryId;

    private String categoryName; // chỉ dùng khi trả về

    @NotNull(message = "Tổng số bản không được để trống")
    @Min(value = 1, message = "Tổng số bản phải lớn hơn 0")
    private Integer totalCopies;

    private Integer availableCopies; // chỉ dùng khi trả về
}
