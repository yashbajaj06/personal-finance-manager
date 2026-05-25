package com.finance.manager.service;

import com.finance.manager.dto.response.MonthlyReportResponse;
import com.finance.manager.dto.response.YearlyReportResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.entity.User;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private User user;
    private Category salary;
    private Category food;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
        salary = Category.builder().id(1L).name("Salary").type(TransactionType.INCOME).isCustom(false).build();
        food = Category.builder().id(2L).name("Food").type(TransactionType.EXPENSE).isCustom(false).build();
    }

    @Test
    void getMonthlyReport_Success() {
        Transaction income = Transaction.builder().id(1L).amount(new BigDecimal("3000"))
                .date(LocalDate.of(2024, 1, 15)).category(salary).user(user).build();
        Transaction expense = Transaction.builder().id(2L).amount(new BigDecimal("500"))
                .date(LocalDate.of(2024, 1, 20)).category(food).user(user).build();

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserAndYearAndMonth(user, 2024, 1))
                .thenReturn(List.of(income, expense));

        MonthlyReportResponse response = reportService.getMonthlyReport("test@example.com", 2024, 1);

        assertEquals(1, response.getMonth());
        assertEquals(2024, response.getYear());
        assertEquals(new BigDecimal("3000"), response.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("500"), response.getTotalExpenses().get("Food"));
        assertEquals(new BigDecimal("2500"), response.getNetSavings());
    }

    @Test
    void getMonthlyReport_Empty() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserAndYearAndMonth(user, 2024, 6)).thenReturn(List.of());

        MonthlyReportResponse response = reportService.getMonthlyReport("test@example.com", 2024, 6);

        assertTrue(response.getTotalIncome().isEmpty());
        assertTrue(response.getTotalExpenses().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getNetSavings());
    }

    @Test
    void getYearlyReport_Success() {
        Transaction income = Transaction.builder().id(1L).amount(new BigDecimal("36000"))
                .date(LocalDate.of(2024, 6, 1)).category(salary).user(user).build();
        Transaction expense = Transaction.builder().id(2L).amount(new BigDecimal("6000"))
                .date(LocalDate.of(2024, 6, 10)).category(food).user(user).build();

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserAndYear(user, 2024)).thenReturn(List.of(income, expense));

        YearlyReportResponse response = reportService.getYearlyReport("test@example.com", 2024);

        assertEquals(2024, response.getYear());
        assertEquals(new BigDecimal("36000"), response.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("6000"), response.getTotalExpenses().get("Food"));
        assertEquals(new BigDecimal("30000"), response.getNetSavings());
    }

    @Test
    void getYearlyReport_MultipleTransactionsSameCategory() {
        Transaction income1 = Transaction.builder().id(1L).amount(new BigDecimal("3000"))
                .date(LocalDate.of(2024, 1, 1)).category(salary).user(user).build();
        Transaction income2 = Transaction.builder().id(2L).amount(new BigDecimal("3000"))
                .date(LocalDate.of(2024, 2, 1)).category(salary).user(user).build();

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserAndYear(user, 2024)).thenReturn(List.of(income1, income2));

        YearlyReportResponse response = reportService.getYearlyReport("test@example.com", 2024);

        assertEquals(new BigDecimal("6000"), response.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("6000"), response.getNetSavings());
    }
}
