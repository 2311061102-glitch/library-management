package vn.edu.library.bookservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.library.bookservice.dto.CategoryDTO;
import vn.edu.library.bookservice.entity.Category;
import vn.edu.library.bookservice.repository.BookRepository;
import vn.edu.library.bookservice.repository.CategoryRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public List<CategoryDTO> getAll() {
        return categoryRepository.findAll().stream().map(this::toDTO).toList();
    }

    public CategoryDTO getById(Long id) {
        return toDTO(find(id));
    }

    public CategoryDTO create(CategoryDTO dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new IllegalArgumentException("Tên thể loại đã tồn tại");
        }
        Category category = new Category();
        category.setName(dto.getName().trim());
        category.setDescription(dto.getDescription());
        return toDTO(categoryRepository.save(category));
    }

    public CategoryDTO update(Long id, CategoryDTO dto) {
        Category category = find(id);
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(dto.getName().trim(), id)) {
            throw new IllegalArgumentException("Tên thể loại đã tồn tại");
        }
        category.setName(dto.getName().trim());
        category.setDescription(dto.getDescription());
        return toDTO(categoryRepository.save(category));
    }

    public void delete(Long id) {
        Category category = find(id);
        if (bookRepository.countByCategoryId(id) > 0) {
            throw new IllegalStateException("Thể loại đang có sách, không thể xóa");
        }
        categoryRepository.delete(category);
    }

    private Category find(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thể loại id = " + id));
    }

    private CategoryDTO toDTO(Category c) {
        return new CategoryDTO(c.getId(), c.getName(), c.getDescription(),
                bookRepository.countByCategoryId(c.getId()));
    }
}
