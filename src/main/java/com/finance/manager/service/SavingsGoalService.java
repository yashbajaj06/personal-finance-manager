package com.finance.manager.service;

import com.finance.manager.dto.request.SavingsGoalRequest;
import com.finance.manager.dto.request.UpdateSavingsGoalRequest;
import com.finance.manager.dto.response.SavingsGoalResponse;

import java.util.List;
import java.util.Map;

/**
 * Manages savings goals and their progress, computed as
 * (total income - total expenses) recorded since the goal's start date.
 */
public interface SavingsGoalService {

    /**
     * Creates a new savings goal for the user.
     *
     * @param username the current user's username
     * @param request  the goal name, target amount, target date, and optional start date
     * @return the created goal, including its initial computed progress
     */
    SavingsGoalResponse createGoal(String username, SavingsGoalRequest request);

    /**
     * @param username the current user's username
     * @return all of this user's savings goals, each with current progress
     */
    List<SavingsGoalResponse> getAllGoals(String username);

    /**
     * Retrieves a single savings goal by id.
     *
     * @param username the current user's username
     * @param id       the goal id
     * @return the goal, including current progress
     * @throws com.finance.manager.exception.ForbiddenException if the goal belongs to another user
     * @throws com.finance.manager.exception.ResourceNotFoundException if no such goal exists
     */
    SavingsGoalResponse getGoalById(String username, Long id);

    /**
     * Updates a goal's target amount and/or target date.
     *
     * @param username the current user's username
     * @param id       the goal id
     * @param request  the fields to update; {@code null} fields are left unchanged
     * @return the updated goal
     * @throws com.finance.manager.exception.ForbiddenException if the goal belongs to another user
     */
    SavingsGoalResponse updateGoal(String username, Long id, UpdateSavingsGoalRequest request);

    /**
     * Deletes a savings goal.
     *
     * @param username the current user's username
     * @param id       the goal id
     * @return a response map containing a confirmation message
     * @throws com.finance.manager.exception.ForbiddenException if the goal belongs to another user
     */
    Map<String, String> deleteGoal(String username, Long id);
}
