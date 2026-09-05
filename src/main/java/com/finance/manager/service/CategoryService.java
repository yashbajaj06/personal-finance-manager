package com.finance.manager.service;

import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.CategoryResponse;

import java.util.List;
import java.util.Map;

/**
 * Manages system default categories and per-user custom categories.
 */
public interface CategoryService {

    /**
     * @param username the current user's username
     * @return all default categories plus this user's custom categories
     */
    List<CategoryResponse> getAllCategories(String username);

    /**
     * Creates a new custom category for the user.
     *
     * @param username the current user's username
     * @param request  the category name and type (INCOME/EXPENSE)
     * @return the created category
     * @throws com.finance.manager.exception.ConflictException if the name is
     *         already used by a default category or one of this user's own categories
     */
    CategoryResponse createCustomCategory(String username, CategoryRequest request);

    /**
     * Deletes one of the user's custom categories.
     *
     * @param username     the current user's username
     * @param categoryName the name of the custom category to delete
     * @return a response map containing a confirmation message
     * @throws com.finance.manager.exception.ForbiddenException if the name refers to a default category
     * @throws com.finance.manager.exception.ValidationException if the category is still referenced by a transaction
     */
    Map<String, String> deleteCustomCategory(String username, String categoryName);
}
