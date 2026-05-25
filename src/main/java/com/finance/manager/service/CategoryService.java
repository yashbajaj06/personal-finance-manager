package com.finance.manager.service;

import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.CategoryResponse;

import java.util.List;
import java.util.Map;

/**
 * Service interface for category management.
 */
public interface CategoryService {

    List<CategoryResponse> getAllCategories(String username);

    CategoryResponse createCustomCategory(String username, CategoryRequest request);

    Map<String, String> deleteCustomCategory(String username, String categoryName);
}
