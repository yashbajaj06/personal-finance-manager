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
 * REST endpoints for viewing system default categories and managing the
 * current user's custom categories.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * @return 200 OK with the system default categories plus the current
     *         user's custom categories, each including its id
     */
    @GetMapping
    public ResponseEntity<Map<String, List<CategoryResponse>>> getAllCategories(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CategoryResponse> categories = categoryService.getAllCategories(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("categories", categories));
    }

    /**
     * Creates a new custom category for the current user.
     *
     * @return 201 Created with the new category, or 409 Conflict if the name is already in use
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCustomCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCustomCategory(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Deletes one of the current user's custom categories.
     *
     * @return 200 OK on success, 403 Forbidden for a default category, or
     *         400 Bad Request if the category is still in use
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, String>> deleteCustomCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String name) {
        Map<String, String> response = categoryService.deleteCustomCategory(userDetails.getUsername(), name);
        return ResponseEntity.ok(response);
    }
}
