package backendjava.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class TransactionRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private java.math.BigDecimal amount;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String comment;

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "INCOME|EXPENSE", message = "Type must be INCOME or EXPENSE")
    private String type;

    public Long getCategoryId() {
        return categoryId;
    }

    public java.math.BigDecimal getAmount() {
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

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setAmount(java.math.BigDecimal amount) {
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
}
