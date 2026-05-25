package com.finance.manager.service.impl;

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
import com.finance.manager.service.SavingsGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of SavingsGoalService.
 * Progress = (Total Income - Total Expenses) since goal start date.
 */
@Service
@RequiredArgsConstructor
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SavingsGoalResponse createGoal(String username, SavingsGoalRequest request) {
        User user = getUser(username);

        if (!request.getTargetDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Target date must be in the future");
        }

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

        SavingsGoal goal = SavingsGoal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .startDate(startDate)
                .user(user)
                .build();

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return toResponse(saved, user);
    }

    @Override
    public List<SavingsGoalResponse> getAllGoals(String username) {
        User user = getUser(username);
        return savingsGoalRepository.findByUser(user).stream()
                .map(g -> toResponse(g, user))
                .collect(Collectors.toList());
    }

    @Override
    public SavingsGoalResponse getGoalById(String username, Long id) {
        User user = getUser(username);
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + id));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied to this goal");
        }

        return toResponse(goal, user);
    }

    @Override
    @Transactional
    public SavingsGoalResponse updateGoal(String username, Long id, UpdateSavingsGoalRequest request) {
        User user = getUser(username);
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + id));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied to this goal");
        }

        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getTargetDate() != null) {
            if (!request.getTargetDate().isAfter(LocalDate.now())) {
                throw new ValidationException("Target date must be in the future");
            }
            goal.setTargetDate(request.getTargetDate());
        }

        SavingsGoal updated = savingsGoalRepository.save(goal);
        return toResponse(updated, user);
    }

    @Override
    @Transactional
    public Map<String, String> deleteGoal(String username, Long id) {
        User user = getUser(username);
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + id));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied to this goal");
        }

        savingsGoalRepository.delete(goal);
        return Map.of("message", "Goal deleted successfully");
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private SavingsGoalResponse toResponse(SavingsGoal goal, User user) {
        BigDecimal income = transactionRepository.sumIncomeByUserSinceDate(user, goal.getStartDate());
        BigDecimal expenses = transactionRepository.sumExpensesByUserSinceDate(user, goal.getStartDate());

        BigDecimal progress = income.subtract(expenses);
        BigDecimal target = goal.getTargetAmount();

        double percentage = 0.0;
        if (target.compareTo(BigDecimal.ZERO) > 0) {
            percentage = progress.divide(target, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            percentage = Math.max(0.0, Math.min(100.0, percentage));
            percentage = Math.round(percentage * 100.0) / 100.0;
        }

        BigDecimal remaining = target.subtract(progress);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        return SavingsGoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(target)
                .targetDate(goal.getTargetDate())
                .startDate(goal.getStartDate())
                .currentProgress(progress)
                .progressPercentage(percentage)
                .remainingAmount(remaining)
                .build();
    }
}
