package backendjava.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class TransactionResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private LocalDate date;
    private String comment;
    private String type;
    private OffsetDateTime createdAt;

    public TransactionResponse(Long id, Long categoryId, String categoryName, BigDecimal amount, LocalDate date, String comment, String type, OffsetDateTime createdAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.amount = amount;
        this.date = date;
        this.comment = comment;
        this.type = type;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getComment() {
        return comment;
    }

    public String getType() {
        return type;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
