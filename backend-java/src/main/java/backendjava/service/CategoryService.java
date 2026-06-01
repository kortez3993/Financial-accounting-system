package backendjava.service;

import backendjava.dto.CategoryRequest;
import backendjava.dto.CategoryResponse;
import backendjava.entity.Category;
import backendjava.entity.User;
import backendjava.exceptions.ResourceNotFoundException;
import backendjava.repository.CategoryRepository;
import backendjava.repository.TransactionRepository;
import backendjava.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository, TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public CategoryResponse createCategory(Long userId, CategoryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = new Category();
        category.setUser(user);
        category.setName(request.getName());
        category.setType(request.getType());
        category.setColor(request.getColor());

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    public CategoryResponse updateCategory(Long userId, Long categoryId, CategoryRequest request) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or doesn't belong to user"));

        category.setName(request.getName());
        category.setType(request.getType());
        category.setColor(request.getColor());

        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or doesn't belong to user"));

        if (transactionRepository.existsByCategoryId(categoryId)) {
            throw new IllegalArgumentException("Cannot delete category with existing transactions");
        }

        categoryRepository.delete(category);
    }

    public List<CategoryResponse> getAllCategories(Long userId) {
        return categoryRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getColor(),
                category.getCreatedAt()
        );
    }
}
