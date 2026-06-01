package backendjava.controller;

import backendjava.dto.BudgetLimitRequest;
import backendjava.dto.BudgetLimitResponse;
import backendjava.service.BudgetLimitService;
import backendjava.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budget-limits")
public class BudgetLimitController {

    public BudgetLimitController(BudgetLimitService budgetLimitService) {
        this.budgetLimitService = budgetLimitService;
    }

    private final BudgetLimitService budgetLimitService;

    @GetMapping
    public List<BudgetLimitResponse> getLimits(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        Long userId = SecurityUtils.getCurrentUserId();
        return budgetLimitService.getLimits(userId, month, year);
    }

    @PostMapping
    public BudgetLimitResponse setLimit(@Valid @RequestBody BudgetLimitRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return budgetLimitService.setLimit(userId, request);
    }

    @DeleteMapping("/{id}")
    public void deleteLimit(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        budgetLimitService.deleteLimit(userId, id);
    }
}
