package vn.edu.library.bookservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.library.bookservice.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
