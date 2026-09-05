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
 * REST endpoints for creating, viewing, updating, and deleting the current
 * user's savings goals.
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    /**
     * @return 201 Created with the new goal, including its initial computed progress
     */
    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SavingsGoalRequest request) {
        SavingsGoalResponse response = savingsGoalService.createGoal(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * @return 200 OK with all of the current user's savings goals
     */
    @GetMapping
    public ResponseEntity<Map<String, List<SavingsGoalResponse>>> getAllGoals(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<SavingsGoalResponse> goals = savingsGoalService.getAllGoals(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("goals", goals));
    }

    /**
     * @return 200 OK with the goal, 403 Forbidden if it belongs to another
     *         user, or 404 Not Found if it does not exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        SavingsGoalResponse response = savingsGoalService.getGoalById(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a goal's target amount and/or target date.
     *
     * @return 200 OK with the updated goal, or 403 Forbidden if it belongs to another user
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
     * @return 200 OK on success, or 403 Forbidden if the goal belongs to another user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Map<String, String> response = savingsGoalService.deleteGoal(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }
}
