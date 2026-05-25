package com.finance.manager.controller;

import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.CategoryResponse;
import com.finance.manager.service.CategoryService;
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
 * Handles category management (default and custom categories).
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Gets all categories (default + user's custom).
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<Map<String, List<CategoryResponse>>> getAllCategories(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CategoryResponse> categories = categoryService.getAllCategories(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("categories", categories));
    }

    /**
     * Creates a custom category for the current user.
     * POST /api/categories
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCustomCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCustomCategory(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Deletes a custom category by name.
     * DELETE /api/categories/{name}
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, String>> deleteCustomCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String name) {
        Map<String, String> response = categoryService.deleteCustomCategory(userDetails.getUsername(), name);
        return ResponseEntity.ok(response);
    }
}
