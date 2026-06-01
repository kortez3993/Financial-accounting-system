package backendjava.controller;

import backendjava.dto.CategoryRequest;
import backendjava.dto.CategoryResponse;
import backendjava.service.CategoryService;
import backendjava.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> getCategories() {
        Long userId = SecurityUtils.getCurrentUserId();
        return categoryService.getAllCategories(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return categoryService.createCategory(userId, request);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return categoryService.updateCategory(userId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        categoryService.deleteCategory(userId, id);
    }
}
