package backendjava.repository;

import backendjava.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
    List<Transaction> findByUserIdAndCategoryIdAndDateBetween(Long userId, Long categoryId, LocalDate start, LocalDate end);
    
    @Query("SELECT SUM(t.amount) FROM transactions t WHERE t.user.id = :userId AND t.type = :type AND t.date BETWEEN :start AND :end")
    BigDecimal sumByUserAndTypeAndDateBetween(@Param("userId") Long userId, @Param("type") String type, @Param("start") LocalDate start, @Param("end") LocalDate end);
    
    @Query("SELECT SUM(t.amount) FROM transactions t WHERE t.user.id = :userId AND t.type = :type")
    BigDecimal sumByUserAndType(@Param("userId") Long userId, @Param("type") String type);
    
    boolean existsByCategoryId(Long categoryId);
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
}
