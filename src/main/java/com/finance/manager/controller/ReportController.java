package com.finance.manager.controller;

import com.finance.manager.dto.response.MonthlyReportResponse;
import com.finance.manager.dto.response.YearlyReportResponse;
import com.finance.manager.exception.ValidationException;
import com.finance.manager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Provides monthly and yearly financial reports.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Gets monthly report for a specific year/month.
     * GET /api/reports/monthly/{year}/{month}
     */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int year,
            @PathVariable int month) {
        if (month < 1 || month > 12) {
            throw new ValidationException("Month must be between 1 and 12");
        }
        MonthlyReportResponse response = reportService.getMonthlyReport(userDetails.getUsername(), year, month);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets yearly report for a specific year.
     * GET /api/reports/yearly/{year}
     */
    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int year) {
        YearlyReportResponse response = reportService.getYearlyReport(userDetails.getUsername(), year);
        return ResponseEntity.ok(response);
    }
}
