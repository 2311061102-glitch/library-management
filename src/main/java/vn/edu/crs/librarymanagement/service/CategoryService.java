package vn.edu.crs.librarymanagement.service;

import org.springframework.stereotype.Service;
import vn.edu.crs.librarymanagement.entity.Category;
import vn.edu.crs.librarymanagement.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // ===== Lấy tất cả Category (ai cũng xem được) =====
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // ===== Lấy Category theo ID (ai cũng xem được) =====
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    // ===== Tạo Category (chỉ ADMIN được phép) =====
    public Category createCategory(String role, Category category) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new SecurityException("Ban khong co quyen tao danh muc");
        }
        return categoryRepository.save(category);
    }

    // ===== Cập nhật Category (chỉ ADMIN được phép) =====
    public Optional<Category> updateCategory(String role, Long id, Category categoryDetails) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new SecurityException("Ban khong co quyen cap nhat danh muc");
        }
        return categoryRepository.findById(id).map(category -> {
            category.setName(categoryDetails.getName());
            return categoryRepository.save(category);
        });
    }

    // ===== Xoá Category (chỉ ADMIN được phép) =====
    public boolean deleteCategory(String role, Long id) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new SecurityException("Ban khong co quyen xoa danh muc");
        }
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}