package backendjava.service;

import backendjava.dto.TransactionRequest;
import backendjava.dto.TransactionResponseWithWarning;
import backendjava.entity.Category;
import backendjava.entity.Transaction;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private BudgetLimitService budgetLimitService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Category expenseCategory;
    private Category incomeCategory;
    private Transaction existingTransaction;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        expenseCategory = new Category();
        expenseCategory.setId(1L);
        expenseCategory.setType("EXPENSE");

        incomeCategory = new Category();
        incomeCategory.setId(2L);
        incomeCategory.setType("INCOME");

        existingTransaction = new Transaction();
        existingTransaction.setId(10L);
        existingTransaction.setUser(user);
        existingTransaction.setCategory(expenseCategory);
        existingTransaction.setAmount(new BigDecimal("1000"));
        existingTransaction.setDate(LocalDate.of(2026, 6, 1));
        existingTransaction.setType("EXPENSE");
    }

    @Test
    void createTransaction_ShouldThrowWhenTypeMismatch() {
        TransactionRequest request = new TransactionRequest();
        request.setCategoryId(1L);
        request.setAmount(BigDecimal.TEN);
        request.setDate(LocalDate.now());
        request.setType("INCOME"); // не совпадает с EXPENSE категорией

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(expenseCategory));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> transactionService.createTransaction(1L, request))
                .withMessage("Transaction type must match category type");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_ShouldSucceed() {
        TransactionRequest request = new TransactionRequest();
        request.setCategoryId(1L);
        request.setAmount(new BigDecimal("100"));
        request.setDate(LocalDate.now());
        request.setType("EXPENSE");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(expenseCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(existingTransaction);
        when(budgetLimitService.checkLimitUsage(anyLong(), anyLong(), anyInt(), anyInt(), any(BigDecimal.class)))
                .thenReturn(new BudgetLimitService.LimitCheckResult(0, false));

        TransactionResponseWithWarning result = transactionService.createTransaction(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getWarning()).isNull();
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void updateTransaction_ShouldThrowWhenTypeMismatch() {
        TransactionRequest request = new TransactionRequest();
        request.setCategoryId(1L);
        request.setAmount(new BigDecimal("500"));
        request.setDate(LocalDate.now());
        request.setType("INCOME");

        when(transactionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existingTransaction));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(expenseCategory));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> transactionService.updateTransaction(1L, 10L, request))
                .withMessage("Transaction type must match category type");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void updateTransaction_ShouldIncludeWarningWhenLimitExceeded() {
        TransactionRequest updateReq = new TransactionRequest();
        updateReq.setCategoryId(1L);
        updateReq.setAmount(new BigDecimal("5000"));
        updateReq.setDate(LocalDate.of(2026, 6, 1));
        updateReq.setType("EXPENSE");

        when(transactionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existingTransaction));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(expenseCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(existingTransaction);

        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq("EXPENSE"), any(), any()))
                .thenReturn(new BigDecimal("5000"));
        when(budgetLimitService.checkLimitUsage(1L, 1L, 6, 2026, new BigDecimal("5000")))
                .thenReturn(new BudgetLimitService.LimitCheckResult(125.0, true));

        TransactionResponseWithWarning result = transactionService.updateTransaction(1L, 10L, updateReq);

        assertThat(result.getWarning()).contains("Бюджет превышен на 25");
    }

    @Test
    void updateTransaction_ShouldSucceedWithoutWarning() {
        TransactionRequest updateReq = new TransactionRequest();
        updateReq.setCategoryId(1L);
        updateReq.setAmount(new BigDecimal("100"));
        updateReq.setDate(LocalDate.of(2026, 6, 1));
        updateReq.setType("EXPENSE");

        when(transactionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existingTransaction));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(expenseCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(existingTransaction);

        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq("EXPENSE"), any(), any()))
                .thenReturn(new BigDecimal("100"));
        when(budgetLimitService.checkLimitUsage(1L, 1L, 6, 2026, new BigDecimal("100")))
                .thenReturn(new BudgetLimitService.LimitCheckResult(2.5, false));

        TransactionResponseWithWarning result = transactionService.updateTransaction(1L, 10L, updateReq);

        assertThat(result.getWarning()).isNull();
    }

    @Test
    void getTotalBalance_ShouldReturnCorrectDifference() {
        when(transactionRepository.sumByUserAndType(1L, "INCOME")).thenReturn(new BigDecimal("10000"));
        when(transactionRepository.sumByUserAndType(1L, "EXPENSE")).thenReturn(new BigDecimal("6000"));

        BigDecimal balance = transactionService.getTotalBalance(1L);

        assertThat(balance).isEqualByComparingTo("4000");
    }

    @Test
    void getCurrentBalance_ShouldReturnCorrectDifference() {
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq("INCOME"), any(), any()))
                .thenReturn(new BigDecimal("5000"));
        when(transactionRepository.sumByUserAndTypeAndDateBetween(eq(1L), eq("EXPENSE"), any(), any()))
                .thenReturn(new BigDecimal("2000"));

        BigDecimal balance = transactionService.getCurrentBalance(1L);

        assertThat(balance).isEqualByComparingTo("3000");
    }

    @Test
    void deleteTransaction_ShouldSucceed() {
        when(transactionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existingTransaction));

        transactionService.deleteTransaction(1L, 10L);

        verify(transactionRepository).delete(existingTransaction);
    }

    @Test
    void deleteTransaction_NotFound_ShouldThrow() {
        when(transactionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
