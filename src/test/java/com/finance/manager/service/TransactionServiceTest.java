package com.finance.manager.service;

import com.finance.manager.dto.request.TransactionRequest;
import com.finance.manager.dto.request.UpdateTransactionRequest;
import com.finance.manager.dto.response.TransactionResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.entity.User;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.exception.ValidationException;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user;
    private Category incomeCategory;
    private Category expenseCategory;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
        incomeCategory = Category.builder().id(1L).name("Salary").type(TransactionType.INCOME).isCustom(false).build();
        expenseCategory = Category.builder().id(2L).name("Food").type(TransactionType.EXPENSE).isCustom(false).build();
    }

    @Test
    void createTransaction_Success() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("5000.00"));
        request.setDate(LocalDate.now().minusDays(1));
        request.setCategory("Salary");
        request.setDescription("January salary");

        Transaction saved = Transaction.builder().id(1L).amount(request.getAmount())
                .date(request.getDate()).category(incomeCategory)
                .description(request.getDescription()).user(user).build();

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndUserOrDefault("Salary", user)).thenReturn(Optional.of(incomeCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        TransactionResponse response = transactionService.createTransaction("test@example.com", request);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.getAmount());
        assertEquals("Salary", response.getCategory());
        assertEquals(TransactionType.INCOME, response.getType());
    }

    @Test
    void createTransaction_FutureDateThrows() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("500.00"));
        request.setDate(LocalDate.now().plusDays(1));
        request.setCategory("Salary");

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class,
                () -> transactionService.createTransaction("test@example.com", request));
    }

    @Test
    void createTransaction_InvalidCategoryThrows() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("500.00"));
        request.setDate(LocalDate.now());
        request.setCategory("NonExistent");

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndUserOrDefault("NonExistent", user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.createTransaction("test@example.com", request));
    }

    @Test
    void getTransactions_NoFilters() {
        Transaction t = Transaction.builder().id(1L).amount(new BigDecimal("1000"))
                .date(LocalDate.now()).category(incomeCategory).user(user).build();

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserOrderByDateDesc(user)).thenReturn(List.of(t));

        List<TransactionResponse> result = transactionService.getTransactions("test@example.com", null, null, null);

        assertEquals(1, result.size());
        assertEquals(new BigDecimal("1000"), result.get(0).getAmount());
    }

    @Test
    void getTransactions_WithDateRange() {
        LocalDate start = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();

        Transaction t = Transaction.builder().id(1L).amount(new BigDecimal("500"))
                .date(LocalDate.now().minusDays(5)).category(expenseCategory).user(user).build();

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserAndDateBetweenOrderByDateDesc(user, start, end)).thenReturn(List.of(t));

        List<TransactionResponse> result = transactionService.getTransactions("test@example.com", start, end, null);
        assertEquals(1, result.size());
    }

    @Test
    void getTransactions_WithCategoryFilter() {
        Transaction t = Transaction.builder().id(1L).amount(new BigDecimal("200"))
                .date(LocalDate.now()).category(expenseCategory).user(user).build();

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(expenseCategory));
        when(transactionRepository.findByUserAndCategoryOrderByDateDesc(user, expenseCategory)).thenReturn(List.of(t));

        List<TransactionResponse> result = transactionService.getTransactions("test@example.com", null, null, 2L);
        assertEquals(1, result.size());
    }

    @Test
    void updateTransaction_Success() {
        Transaction existing = Transaction.builder().id(1L).amount(new BigDecimal("1000"))
                .date(LocalDate.now().minusDays(1)).category(incomeCategory).user(user).build();

        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setAmount(new BigDecimal("1500"));
        request.setDescription("Updated desc");

        Transaction updated = Transaction.builder().id(1L).amount(new BigDecimal("1500"))
                .date(LocalDate.now().minusDays(1)).category(incomeCategory).description("Updated desc").user(user).build();

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(any())).thenReturn(updated);

        TransactionResponse response = transactionService.updateTransaction("test@example.com", 1L, request);

        assertEquals(new BigDecimal("1500"), response.getAmount());
        assertEquals("Updated desc", response.getDescription());
    }

    @Test
    void updateTransaction_NotFoundThrows() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.updateTransaction("test@example.com", 99L, new UpdateTransactionRequest()));
    }

    @Test
    void deleteTransaction_Success() {
        Transaction t = Transaction.builder().id(1L).user(user).category(incomeCategory).build();

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(t));

        Map<String, String> result = transactionService.deleteTransaction("test@example.com", 1L);

        assertEquals("Transaction deleted successfully", result.get("message"));
        verify(transactionRepository).delete(t);
    }

    @Test
    void deleteTransaction_NotFoundThrows() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.deleteTransaction("test@example.com", 99L));
    }
}
