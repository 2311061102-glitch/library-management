package com.example.bookexchange.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(length = 100)
    private String category;

    private String bookCondition;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BookListingType listingType;

    @Column(length = 2000)
    private String description;

    @PrePersist
    private void setDefaultListingType() {
        if (listingType == null) {
            listingType = BookListingType.BAN;
        }
    }
}
