package com.finance.manager.service.impl;

import com.finance.manager.dto.response.MonthlyReportResponse;
import com.finance.manager.dto.response.YearlyReportResponse;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.entity.User;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@inheritDoc}
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public MonthlyReportResponse getMonthlyReport(String username, int year, int month) {
        User user = getUser(username);

        List<Transaction> transactions = transactionRepository.findByUserAndYearAndMonth(user, year, month);

        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            if (t.getCategory().getType() == TransactionType.INCOME) {
                incomeByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            } else {
                expensesByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            }
        }

        BigDecimal totalIncome = incomeByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expensesByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return MonthlyReportResponse.builder()
                .month(month)
                .year(year)
                .totalIncome(incomeByCategory)
                .totalExpenses(expensesByCategory)
                .netSavings(netSavings)
                .build();
    }

    @Override
    public YearlyReportResponse getYearlyReport(String username, int year) {
        User user = getUser(username);

        List<Transaction> transactions = transactionRepository.findByUserAndYear(user, year);

        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            if (t.getCategory().getType() == TransactionType.INCOME) {
                incomeByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            } else {
                expensesByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            }
        }

        BigDecimal totalIncome = incomeByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expensesByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return YearlyReportResponse.builder()
                .year(year)
                .totalIncome(incomeByCategory)
                .totalExpenses(expensesByCategory)
                .netSavings(netSavings)
                .build();
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
