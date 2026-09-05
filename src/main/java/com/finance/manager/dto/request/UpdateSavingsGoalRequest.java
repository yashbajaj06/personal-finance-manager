package com.finance.manager.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for updating a savings goal's target amount and/or date.
 * All fields are optional; only non-null fields are applied.
 */
@Data
public class UpdateSavingsGoalRequest {

    @DecimalMin(value = "0.01", message = "Target amount must be positive")
    private BigDecimal targetAmount;

    private LocalDate targetDate;
}
