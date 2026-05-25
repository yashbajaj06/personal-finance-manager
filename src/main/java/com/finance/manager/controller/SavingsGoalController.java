package com.finance.manager.controller;

import com.finance.manager.dto.request.SavingsGoalRequest;
import com.finance.manager.dto.request.UpdateSavingsGoalRequest;
import com.finance.manager.dto.response.SavingsGoalResponse;
import com.finance.manager.service.SavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Handles savings goals CRUD and progress tracking.
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    /**
     * Creates a new savings goal.
     * POST /api/goals
     */
    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SavingsGoalRequest request) {
        SavingsGoalResponse response = savingsGoalService.createGoal(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets all savings goals for the current user.
     * GET /api/goals
     */
    @GetMapping
    public ResponseEntity<Map<String, List<SavingsGoalResponse>>> getAllGoals(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<SavingsGoalResponse> goals = savingsGoalService.getAllGoals(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("goals", goals));
    }

    /**
     * Gets a specific savings goal by ID.
     * GET /api/goals/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        SavingsGoalResponse response = savingsGoalService.getGoalById(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates target amount and/or date of a goal.
     * PUT /api/goals/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> updateGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateSavingsGoalRequest request) {
        SavingsGoalResponse response = savingsGoalService.updateGoal(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a savings goal.
     * DELETE /api/goals/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Map<String, String> response = savingsGoalService.deleteGoal(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }
}
