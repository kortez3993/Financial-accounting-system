package backendjava.service;

import backendjava.dto.CategoryRequest;
import backendjava.dto.CategoryResponse;
import backendjava.entity.Category;
import backendjava.entity.User;
import backendjava.exceptions.ResourceNotFoundException;
import backendjava.repository.CategoryRepository;
import backendjava.repository.TransactionRepository;
import backendjava.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setUser(testUser);
        testCategory.setName("Еда");
        testCategory.setType("EXPENSE");
        testCategory.setColor("#FF0000");
    }

    @Test
    void deleteCategory_WithoutTransactions_ShouldSucceed() {
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.deleteCategory(1L, 1L);

        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void deleteCategory_WithTransactions_ShouldThrow() {
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> categoryService.deleteCategory(1L, 1L))
                .withMessage("Cannot delete category with existing transactions");

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteCategory_NotFound_ShouldThrow() {
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createCategory_ShouldSucceed() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Транспорт");
        request.setType("EXPENSE");
        request.setColor("#00FF00");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponse response = categoryService.createCategory(1L, request);

        assertThat(response).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldSucceed() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Еда (обновлено)");
        request.setType("EXPENSE");
        request.setColor("#FF00FF");

        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponse response = categoryService.updateCategory(1L, 1L, request);

        assertThat(response).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void getAllCategories_ShouldReturnList() {
        when(categoryRepository.findByUserId(1L)).thenReturn(java.util.List.of(testCategory));

        var categories = categoryService.getAllCategories(1L);

        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).getName()).isEqualTo("Еда");
    }
}
