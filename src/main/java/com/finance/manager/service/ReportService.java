package com.finance.manager.service;

import com.finance.manager.dto.response.MonthlyReportResponse;
import com.finance.manager.dto.response.YearlyReportResponse;

/**
 * Service interface for financial reports.
 */
public interface ReportService {

    MonthlyReportResponse getMonthlyReport(String username, int year, int month);

    YearlyReportResponse getYearlyReport(String username, int year);
}
