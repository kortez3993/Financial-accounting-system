package backendjava.service;

import backendjava.dto.TransactionRequest;
import backendjava.dto.TransactionResponse;
import backendjava.dto.TransactionResponseWithWarning;
import backendjava.entity.Category;
import backendjava.entity.Transaction;
import backendjava.entity.User;
import backendjava.exceptions.ResourceNotFoundException;
import backendjava.repository.CategoryRepository;
import backendjava.repository.TransactionRepository;
import backendjava.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository, CategoryRepository categoryRepository, BudgetLimitService budgetLimitService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.budgetLimitService = budgetLimitService;
    }

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetLimitService budgetLimitService;

    public TransactionResponseWithWarning createTransaction(Long userId, TransactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or doesn't belong to user"));

        if (!request.getType().equals(category.getType())) {
            throw new IllegalArgumentException("Transaction type must match category type");
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate());
        transaction.setComment(request.getComment());
        transaction.setType(request.getType());

        Transaction saved = transactionRepository.save(transaction);
        TransactionResponse response = mapToResponse(saved);

        String warning = null;
        if ("EXPENSE".equals(request.getType())) {
            int month = request.getDate().getMonthValue();
            int year = request.getDate().getYear();

            LocalDate startOfMonth = YearMonth.of(year, month).atDay(1);
            LocalDate endOfMonth = YearMonth.of(year, month).atEndOfMonth();

            BigDecimal totalSpent = transactionRepository.sumByUserAndTypeAndDateBetween(
                    userId, "EXPENSE", startOfMonth, endOfMonth);
            if (totalSpent == null) totalSpent = BigDecimal.ZERO;

            BudgetLimitService.LimitCheckResult checkResult = budgetLimitService.checkLimitUsage(
                    userId, request.getCategoryId(), month, year, totalSpent);

            if (checkResult.warning()) {
                if (checkResult.percentage() >= 100) {
                    warning = String.format("Бюджет превышен на %.1f%%", checkResult.percentage() - 100);
                } else {
                    warning = String.format("Использовано %.1f%% бюджета", checkResult.percentage());
                }
            }
        }

        return new TransactionResponseWithWarning(response, warning);
    }

    public TransactionResponseWithWarning updateTransaction(Long userId, Long transactionId, TransactionRequest request) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found or doesn't belong to user"));

        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or doesn't belong to user"));

        if (!request.getType().equals(category.getType())) {
            throw new IllegalArgumentException("Transaction type must match category type");
        }

        transaction.setCategory(category);
        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate());
        transaction.setComment(request.getComment());
        transaction.setType(request.getType());

        Transaction updated = transactionRepository.save(transaction);
        TransactionResponse response = mapToResponse(updated);

        String warning = null;
        if ("EXPENSE".equals(request.getType())) {
            int month = request.getDate().getMonthValue();
            int year = request.getDate().getYear();

            LocalDate startOfMonth = YearMonth.of(year, month).atDay(1);
            LocalDate endOfMonth = YearMonth.of(year, month).atEndOfMonth();

            BigDecimal totalSpent = transactionRepository.sumByUserAndTypeAndDateBetween(
                    userId, "EXPENSE", startOfMonth, endOfMonth);
            if (totalSpent == null) totalSpent = BigDecimal.ZERO;

            BudgetLimitService.LimitCheckResult checkResult = budgetLimitService.checkLimitUsage(
                    userId, request.getCategoryId(), month, year, totalSpent);

            if (checkResult.warning()) {
                if (checkResult.percentage() >= 100) {
                    warning = String.format("Бюджет превышен на %.1f%%", checkResult.percentage() - 100);
                } else {
                    warning = String.format("Использовано %.1f%% бюджета", checkResult.percentage());
                }
            }
        }

        return new TransactionResponseWithWarning(response, warning);
    }

    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found or doesn't belong to user"));
        transactionRepository.delete(transaction);
    }

    public List<TransactionResponse> getTransactions(Long userId, LocalDate from, LocalDate to, Long categoryId, String type) {
        List<Transaction> transactions;

        if (from != null && to != null && categoryId != null && type != null) {
            transactions = transactionRepository.findByUserIdAndCategoryIdAndDateBetween(userId, categoryId, from, to)
                    .stream()
                    .filter(t -> t.getType().equals(type))
                    .collect(Collectors.toList());
        } else if (from != null && to != null) {
            transactions = transactionRepository.findByUserIdAndDateBetween(userId, from, to);
        } else {
            transactions = transactionRepository.findByUserId(userId);
        }

        if (categoryId != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        if (type != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getType().equals(type))
                    .collect(Collectors.toList());
        }

        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BigDecimal getCurrentBalance(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = YearMonth.from(now).atDay(1);
        LocalDate endOfMonth = YearMonth.from(now).atEndOfMonth();

        BigDecimal income = transactionRepository.sumByUserAndTypeAndDateBetween(userId, "INCOME", startOfMonth, endOfMonth);
        BigDecimal expense = transactionRepository.sumByUserAndTypeAndDateBetween(userId, "EXPENSE", startOfMonth, endOfMonth);

        if (income == null) income = BigDecimal.ZERO;
        if (expense == null) expense = BigDecimal.ZERO;

        return income.subtract(expense);
    }

    public BigDecimal getTotalBalance(Long userId) {
        BigDecimal income = transactionRepository.sumByUserAndType(userId, "INCOME");
        BigDecimal expense = transactionRepository.sumByUserAndType(userId, "EXPENSE");

        if (income == null) income = BigDecimal.ZERO;
        if (expense == null) expense = BigDecimal.ZERO;

        return income.subtract(expense);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getComment(),
                transaction.getType(),
                transaction.getCreatedAt()
        );
    }
}
