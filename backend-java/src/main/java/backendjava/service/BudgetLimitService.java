package backendjava.service;

import backendjava.dto.BudgetLimitRequest;
import backendjava.dto.BudgetLimitResponse;
import backendjava.entity.BudgetLimit;
import backendjava.entity.Category;
import backendjava.entity.User;
import backendjava.exceptions.ResourceNotFoundException;
import backendjava.repository.BudgetLimitRepository;
import backendjava.repository.CategoryRepository;
import backendjava.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetLimitService {

    public BudgetLimitService(BudgetLimitRepository budgetLimitRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.budgetLimitRepository = budgetLimitRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    private final BudgetLimitRepository budgetLimitRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public BudgetLimitResponse setLimit(Long userId, BudgetLimitRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or doesn't belong to user"));

        BudgetLimit budgetLimit = budgetLimitRepository
                .findByUserIdAndCategoryIdAndMonthAndYear(userId, request.getCategoryId(), request.getMonth(), request.getYear())
                .orElse(new BudgetLimit());

        budgetLimit.setUser(user);
        budgetLimit.setCategory(category);
        budgetLimit.setLimitAmount(request.getLimitAmount());
        budgetLimit.setMonth(request.getMonth());
        budgetLimit.setYear(request.getYear());

        BudgetLimit saved = budgetLimitRepository.save(budgetLimit);
        return mapToResponse(saved);
    }

    public BudgetLimitResponse getLimit(Long userId, Long categoryId, Integer month, Integer year) {
        BudgetLimit budgetLimit = budgetLimitRepository
                .findByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, month, year)
                .orElseThrow(() -> new ResourceNotFoundException("Budget limit not found"));
        return mapToResponse(budgetLimit);
    }

    public List<BudgetLimitResponse> getLimits(Long userId, Integer month, Integer year) {
        return budgetLimitRepository.findByUserIdAndYearAndMonth(userId, year, month).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteLimit(Long userId, Long limitId) {
        BudgetLimit budgetLimit = budgetLimitRepository.findByIdAndUserId(limitId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget limit not found or doesn't belong to user"));
        budgetLimitRepository.delete(budgetLimit);
    }

    public LimitCheckResult checkLimitUsage(Long userId, Long categoryId, Integer month, Integer year, BigDecimal currentSpent) {
        return budgetLimitRepository
                .findByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, month, year)
                .map(limit -> {
                    BigDecimal limitAmount = limit.getLimitAmount();
                    double percentage = currentSpent.divide(limitAmount, 4, RoundingMode.HALF_UP).doubleValue() * 100;
                    boolean warning = percentage >= 80;
                    return new LimitCheckResult(percentage, warning);
                })
                .orElse(new LimitCheckResult(0, false));
    }

    private BudgetLimitResponse mapToResponse(BudgetLimit budgetLimit) {
        return new BudgetLimitResponse(
                budgetLimit.getId(),
                budgetLimit.getCategory().getId(),
                budgetLimit.getCategory().getName(),
                budgetLimit.getLimitAmount(),
                budgetLimit.getMonth(),
                budgetLimit.getYear()
        );
    }

    public record LimitCheckResult(double percentage, boolean warning) {}
}
