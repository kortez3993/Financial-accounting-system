package backendjava.repository;

import backendjava.entity.BudgetLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetLimitRepository extends JpaRepository<BudgetLimit, Long> {
    Optional<BudgetLimit> findByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);
    List<BudgetLimit> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);
    Optional<BudgetLimit> findByIdAndUserId(Long id, Long userId);
}
