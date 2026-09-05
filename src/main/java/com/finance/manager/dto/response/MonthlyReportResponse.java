package com.finance.manager.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response body for the monthly income/expense report, broken down by category.
 */
@Data
@Builder
public class MonthlyReportResponse {
    private int month;
    private int year;
    private Map<String, BigDecimal> totalIncome;
    private Map<String, BigDecimal> totalExpenses;
    private BigDecimal netSavings;
}
