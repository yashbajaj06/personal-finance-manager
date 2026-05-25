package com.finance.manager.service;

import com.finance.manager.dto.request.SavingsGoalRequest;
import com.finance.manager.dto.request.UpdateSavingsGoalRequest;
import com.finance.manager.dto.response.SavingsGoalResponse;
import com.finance.manager.entity.SavingsGoal;
import com.finance.manager.entity.User;
import com.finance.manager.exception.ForbiddenException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.exception.ValidationException;
import com.finance.manager.repository.SavingsGoalRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.impl.SavingsGoalServiceImpl;
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
class SavingsGoalServiceTest {

    @Mock private SavingsGoalRepository savingsGoalRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private SavingsGoalServiceImpl savingsGoalService;

    private User user;
    private SavingsGoal goal;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
        goal = SavingsGoal.builder()
                .id(1L).goalName("Emergency Fund")
                .targetAmount(new BigDecimal("10000"))
                .targetDate(LocalDate.now().plusMonths(6))
                .startDate(LocalDate.now())
                .user(user).build();
    }

    @Test
    void createGoal_Success() {
        SavingsGoalRequest request = new SavingsGoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(new BigDecimal("10000"));
        request.setTargetDate(LocalDate.now().plusMonths(6));

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.save(any())).thenReturn(goal);
        when(transactionRepository.sumIncomeByUserSinceDate(any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumExpensesByUserSinceDate(any(), any())).thenReturn(BigDecimal.ZERO);

        SavingsGoalResponse response = savingsGoalService.createGoal("test@example.com", request);

        assertNotNull(response);
        assertEquals("Emergency Fund", response.getGoalName());
        assertEquals(new BigDecimal("10000"), response.getTargetAmount());
    }

    @Test
    void createGoal_PastTargetDateThrows() {
        SavingsGoalRequest request = new SavingsGoalRequest();
        request.setGoalName("Old Goal");
        request.setTargetAmount(new BigDecimal("5000"));
        request.setTargetDate(LocalDate.now().minusDays(1));

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class,
                () -> savingsGoalService.createGoal("test@example.com", request));
    }

    @Test
    void getAllGoals_Success() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findByUser(user)).thenReturn(List.of(goal));
        when(transactionRepository.sumIncomeByUserSinceDate(any(), any())).thenReturn(new BigDecimal("2000"));
        when(transactionRepository.sumExpensesByUserSinceDate(any(), any())).thenReturn(new BigDecimal("500"));

        List<SavingsGoalResponse> result = savingsGoalService.getAllGoals("test@example.com");

        assertEquals(1, result.size());
        assertEquals(new BigDecimal("1500"), result.get(0).getCurrentProgress());
        assertEquals(15.0, result.get(0).getProgressPercentage());
    }

    @Test
    void getGoalById_Success() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(transactionRepository.sumIncomeByUserSinceDate(any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumExpensesByUserSinceDate(any(), any())).thenReturn(BigDecimal.ZERO);

        SavingsGoalResponse response = savingsGoalService.getGoalById("test@example.com", 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getGoalById_OtherUserThrowsForbidden() {
        User otherUser = User.builder().id(2L).username("other@example.com").build();
        SavingsGoal otherGoal = SavingsGoal.builder().id(1L).user(otherUser)
                .targetAmount(BigDecimal.TEN).targetDate(LocalDate.now().plusYears(1))
                .startDate(LocalDate.now()).goalName("Other").build();

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(otherGoal));

        assertThrows(ForbiddenException.class,
                () -> savingsGoalService.getGoalById("test@example.com", 1L));
    }

    @Test
    void updateGoal_Success() {
        UpdateSavingsGoalRequest request = new UpdateSavingsGoalRequest();
        request.setTargetAmount(new BigDecimal("15000"));
        request.setTargetDate(LocalDate.now().plusYears(1));

        SavingsGoal updated = SavingsGoal.builder().id(1L).goalName("Emergency Fund")
                .targetAmount(new BigDecimal("15000")).targetDate(LocalDate.now().plusYears(1))
                .startDate(LocalDate.now()).user(user).build();

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(savingsGoalRepository.save(any())).thenReturn(updated);
        when(transactionRepository.sumIncomeByUserSinceDate(any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumExpensesByUserSinceDate(any(), any())).thenReturn(BigDecimal.ZERO);

        SavingsGoalResponse response = savingsGoalService.updateGoal("test@example.com", 1L, request);

        assertEquals(new BigDecimal("15000"), response.getTargetAmount());
    }

    @Test
    void updateGoal_PastDateThrows() {
        UpdateSavingsGoalRequest request = new UpdateSavingsGoalRequest();
        request.setTargetDate(LocalDate.now().minusDays(1));

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(ValidationException.class,
                () -> savingsGoalService.updateGoal("test@example.com", 1L, request));
    }

    @Test
    void deleteGoal_Success() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        Map<String, String> result = savingsGoalService.deleteGoal("test@example.com", 1L);

        assertEquals("Goal deleted successfully", result.get("message"));
        verify(savingsGoalRepository).delete(goal);
    }

    @Test
    void deleteGoal_NotFoundThrows() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> savingsGoalService.deleteGoal("test@example.com", 99L));
    }

    @Test
    void deleteGoal_OtherUserThrowsForbidden() {
        User otherUser = User.builder().id(2L).username("other@example.com").build();
        SavingsGoal otherGoal = SavingsGoal.builder().id(1L).user(otherUser)
                .targetAmount(BigDecimal.TEN).targetDate(LocalDate.now().plusYears(1))
                .startDate(LocalDate.now()).goalName("Other").build();

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(otherGoal));

        assertThrows(ForbiddenException.class,
                () -> savingsGoalService.deleteGoal("test@example.com", 1L));
    }
}
