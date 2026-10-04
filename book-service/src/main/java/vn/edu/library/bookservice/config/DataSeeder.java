package vn.edu.library.bookservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.edu.library.bookservice.entity.Book;
import vn.edu.library.bookservice.entity.Category;
import vn.edu.library.bookservice.repository.BookRepository;
import vn.edu.library.bookservice.repository.CategoryRepository;

/** Dữ liệu mẫu để demo: chỉ chạy khi bảng category còn trống. */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) return;

        Category it = category("Công nghệ thông tin", "Lập trình, kiến trúc phần mềm, cơ sở dữ liệu");
        Category lit = category("Văn học", "Tiểu thuyết, truyện ngắn, thơ");
        Category sci = category("Khoa học", "Khoa học tự nhiên và đời sống");
        Category eco = category("Kinh tế", "Kinh tế, quản trị, khởi nghiệp");

        book("Clean Code", "Robert C. Martin", "9780132350884", 2008, it, 5);
        book("Design Patterns", "Erich Gamma", "9780201633610", 1994, it, 3);
        book("Building Microservices", "Sam Newman", "9781492034025", 2021, it, 4);
        book("Designing Data-Intensive Applications", "Martin Kleppmann", "9781449373320", 2017, it, 2);
        book("Dế Mèn phiêu lưu ký", "Tô Hoài", "9786041000011", 1941, lit, 6);
        book("Số đỏ", "Vũ Trọng Phụng", "9786041000028", 1936, lit, 3);
        book("Lược sử thời gian", "Stephen Hawking", "9780553380163", 1988, sci, 2);
        book("Sapiens: Lược sử loài người", "Yuval Noah Harari", "9780062316097", 2011, sci, 4);
        book("Cha giàu cha nghèo", "Robert Kiyosaki", "9781612680194", 1997, eco, 5);
        book("Khởi nghiệp tinh gọn", "Eric Ries", "9780307887894", 2011, eco, 1);
    }

    private Category category(String name, String description) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        return categoryRepository.save(c);
    }

    private void book(String title, String author, String isbn, int year, Category category, int copies) {
        Book b = new Book();
        b.setTitle(title);
        b.setAuthor(author);
        b.setIsbn(isbn);
        b.setPublishYear(year);
        b.setCategory(category);
        b.setTotalCopies(copies);
        b.setAvailableCopies(copies);
        bookRepository.save(b);
    }
}
