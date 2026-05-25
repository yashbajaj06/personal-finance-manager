package com.finance.manager.service;

import com.finance.manager.dto.request.SavingsGoalRequest;
import com.finance.manager.dto.request.UpdateSavingsGoalRequest;
import com.finance.manager.dto.response.SavingsGoalResponse;

import java.util.List;
import java.util.Map;

/**
 * Service interface for savings goal management.
 */
public interface SavingsGoalService {

    SavingsGoalResponse createGoal(String username, SavingsGoalRequest request);

    List<SavingsGoalResponse> getAllGoals(String username);

    SavingsGoalResponse getGoalById(String username, Long id);

    SavingsGoalResponse updateGoal(String username, Long id, UpdateSavingsGoalRequest request);

    Map<String, String> deleteGoal(String username, Long id);
}
