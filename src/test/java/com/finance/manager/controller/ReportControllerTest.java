package com.finance.manager.controller;

import com.finance.manager.dto.response.MonthlyReportResponse;
import com.finance.manager.dto.response.YearlyReportResponse;
import com.finance.manager.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@Import(com.finance.manager.config.SecurityConfig.class)
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ReportService reportService;
    @MockBean private com.finance.manager.security.CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "user@example.com")
    void getMonthlyReport_Success() throws Exception {
        MonthlyReportResponse response = MonthlyReportResponse.builder()
                .month(1).year(2024)
                .totalIncome(Map.of("Salary", new BigDecimal("3000")))
                .totalExpenses(Map.of("Food", new BigDecimal("500")))
                .netSavings(new BigDecimal("2500")).build();

        when(reportService.getMonthlyReport(any(), eq(2024), eq(1))).thenReturn(response);

        mockMvc.perform(get("/api/reports/monthly/2024/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(1))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.netSavings").value(2500));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getMonthlyReport_InvalidMonth_Returns400() throws Exception {
        mockMvc.perform(get("/api/reports/monthly/2024/13"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getYearlyReport_Success() throws Exception {
        YearlyReportResponse response = YearlyReportResponse.builder()
                .year(2024)
                .totalIncome(Map.of("Salary", new BigDecimal("36000")))
                .totalExpenses(Map.of("Food", new BigDecimal("6000")))
                .netSavings(new BigDecimal("30000")).build();

        when(reportService.getYearlyReport(any(), eq(2024))).thenReturn(response);

        mockMvc.perform(get("/api/reports/yearly/2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.netSavings").value(30000));
    }

    @Test
    void getMonthlyReport_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/reports/monthly/2024/1"))
                .andExpect(status().isUnauthorized());
    }
}
