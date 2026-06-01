package backendjava.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity(name = "budget_limits")
public class BudgetLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "limit_amount", nullable = false)
    private BigDecimal limitAmount;

    private Integer month;
    private Integer year;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public Integer getMonth() {
        return month;
    }

    public Integer getYear() {
        return year;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BudgetLimit() {
    }

    public BudgetLimit(Long id, User user, Category category, BigDecimal limitAmount, Integer month, Integer year) {
        this.id = id;
        this.user = user;
        this.category = category;
        this.limitAmount = limitAmount;
        this.month = month;
        this.year = year;
    }
}
