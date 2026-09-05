package com.finance.manager.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for creating a new savings goal.
 */
@Data
public class SavingsGoalRequest {

    @NotBlank(message = "Goal name is required")
    private String goalName;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be positive")
    @DecimalMin(value = "0.01", message = "Target amount must be positive")
    private BigDecimal targetAmount;

    @NotNull(message = "Target date is required")
    private LocalDate targetDate;

    private LocalDate startDate;
}
