package com.finance.manager.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response body for savings goal data including computed progress fields.
 */
@Data
@Builder
public class SavingsGoalResponse {
    private Long id;
    private String goalName;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private LocalDate startDate;
    private BigDecimal currentProgress;
    private Double progressPercentage;
    private BigDecimal remainingAmount;
}
