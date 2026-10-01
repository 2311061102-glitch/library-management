package vn.edu.crs.librarymanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "book_copies", uniqueConstraints = @UniqueConstraint(name = "uk_book_copy_barcode", columnNames = "barcode"))
public class BookCopy {
    public static final String AVAILABLE = "AVAILABLE";
    public static final String BORROWED = "BORROWED";
    public static final String RESERVED = "RESERVED";
    public static final String DAMAGED = "DAMAGED";
    public static final String LOST = "LOST";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnore
    private Book book;

    @Column(nullable = false, length = 20)
    private String status = AVAILABLE;

    public Long getId() { return id; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
