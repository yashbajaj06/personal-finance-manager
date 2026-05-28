package com.finance.manager.service.impl;

import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.CategoryResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import com.finance.manager.exception.ConflictException;
import com.finance.manager.exception.ForbiddenException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.exception.ValidationException;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public List<CategoryResponse> getAllCategories(String username) {
        User user = getUser(username);
        List<Category> defaults = categoryRepository.findByUserIsNull();
        List<Category> custom = categoryRepository.findByUser(user);
        List<CategoryResponse> result = new ArrayList<>();
        defaults.stream().map(this::toResponse).forEach(result::add);
        custom.stream().map(this::toResponse).forEach(result::add);
        return result;
    }

    @Override
    @Transactional
    public CategoryResponse createCustomCategory(String username, CategoryRequest request) {
        User user = getUser(username);
        if (categoryRepository.existsByNameAndUser(request.getName(), user)) {
            throw new ConflictException("Custom category already exists: " + request.getName());
        }
        if (categoryRepository.findByNameAndUserIsNull(request.getName()).isPresent()) {
            throw new ConflictException("A default category with this name already exists: " + request.getName());
        }
        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .user(user)
                .isCustom(true)
                .build();
        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public Map<String, String> deleteCustomCategory(String username, String categoryName) {
        User user = getUser(username);
        if (categoryRepository.findByNameAndUserIsNull(categoryName).isPresent()) {
            throw new ForbiddenException("Default categories cannot be deleted");
        }
        Category category = categoryRepository.findByNameAndUser(categoryName, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryName));
        if (transactionRepository.existsByCategory(category)) {
            throw new ValidationException("Cannot delete category that is currently used by transactions");
        }
        categoryRepository.delete(category);
        return Map.of("message", "Category deleted successfully");
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .name(category.getName())
                .type(category.getType())
                .isCustom(category.isCustom())
                .build();
    }
}
