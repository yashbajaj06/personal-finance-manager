package com.finance.manager.service;

import com.finance.manager.dto.response.MonthlyReportResponse;
import com.finance.manager.dto.response.YearlyReportResponse;

/**
 * Produces income/expense reports aggregated by category.
 */
public interface ReportService {

    /**
     * Builds an income/expense breakdown for a single month.
     *
     * @param username the current user's username
     * @param year     the calendar year
     * @param month    the calendar month (1-12)
     * @return total income and expenses per category, plus net savings, for that month
     */
    MonthlyReportResponse getMonthlyReport(String username, int year, int month);

    /**
     * Builds an income/expense breakdown for an entire year.
     *
     * @param username the current user's username
     * @param year     the calendar year
     * @return total income and expenses per category, plus net savings, for that year
     */
    YearlyReportResponse getYearlyReport(String username, int year);
}
