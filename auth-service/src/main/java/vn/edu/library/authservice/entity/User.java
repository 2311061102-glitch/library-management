package vn.edu.library.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    public static final String ROLE_READER = "READER";       // độc giả
    public static final String ROLE_LIBRARIAN = "LIBRARIAN"; // thủ thư

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String password; // luôn lưu dạng đã mã hóa BCrypt, không bao giờ lưu plain text

    @Column(nullable = false, length = 20)
    private String role; // "READER" hoặc "LIBRARIAN"
}
