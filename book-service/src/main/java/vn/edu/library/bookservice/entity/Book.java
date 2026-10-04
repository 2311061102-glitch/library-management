package vn.edu.library.bookservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(nullable = false, unique = true, length = 30)
    private String isbn;

    @Column(name = "publish_year")
    private Integer publishYear;

    // Quan hệ N-1 tới Category (cùng database book_db nên dùng được khóa ngoại thật)
    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "total_copies", nullable = false)
    private Integer totalCopies;

    // thay cho soChoConLai của CRS: số bản sách còn trên kệ, có thể cho mượn
    @Column(name = "available_copies", nullable = false)
    private Integer availableCopies;
}
