package backendjava.controller;

import backendjava.dto.TransactionRequest;
import backendjava.dto.TransactionResponse;
import backendjava.dto.TransactionResponseWithWarning;
import backendjava.service.TransactionService;
import backendjava.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    private final TransactionService transactionService;

    @GetMapping
    public List<TransactionResponse> getTransactions(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String type) {
        Long userId = SecurityUtils.getCurrentUserId();
        return transactionService.getTransactions(userId, from, to, categoryId, type);
    }

    @PostMapping
    public TransactionResponseWithWarning create(@Valid @RequestBody TransactionRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return transactionService.createTransaction(userId, request);
    }

    @PutMapping("/{id}")
    public TransactionResponseWithWarning update(@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return transactionService.updateTransaction(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        transactionService.deleteTransaction(userId, id);
    }

    @GetMapping("/balance")
    public Map<String, BigDecimal> getBalance() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Map.of("balance", transactionService.getCurrentBalance(userId));
    }

    @GetMapping("/total-balance")
    public Map<String, BigDecimal> getTotalBalance() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Map.of("balance", transactionService.getTotalBalance(userId));
    }
}
