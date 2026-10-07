package vn.edu.library.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Hồ sơ độc giả (thay cho Student của hệ thống CRS). */
@Entity
@Table(name = "reader")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    // Mã độc giả (thay cho studentCode/mssv), ví dụ: DG000001
    @Column(name = "reader_code", unique = true, length = 100)
    private String readerCode;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;
}
