package com.finance.manager.service;

import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.CategoryResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.entity.User;
import com.finance.manager.exception.ConflictException;
import com.finance.manager.exception.ForbiddenException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.exception.ValidationException;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private User user;
    private Category defaultCat;
    private Category customCat;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
        defaultCat = Category.builder().id(1L).name("Salary").type(TransactionType.INCOME).isCustom(false).build();
        customCat = Category.builder().id(10L).name("MyCustom").type(TransactionType.EXPENSE).isCustom(true).user(user).build();
    }

    @Test
    void getAllCategories_ReturnsBothDefaultAndCustom() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByUserIsNull()).thenReturn(List.of(defaultCat));
        when(categoryRepository.findByUser(user)).thenReturn(List.of(customCat));

        List<CategoryResponse> result = categoryService.getAllCategories("test@example.com");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Salary")));
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("MyCustom")));
    }

    @Test
    void createCustomCategory_Success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("SideIncome");
        request.setType(TransactionType.INCOME);

        Category saved = Category.builder().id(11L).name("SideIncome").type(TransactionType.INCOME).isCustom(true).user(user).build();

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.existsByNameAndUser("SideIncome", user)).thenReturn(false);
        when(categoryRepository.findByNameAndUserIsNull("SideIncome")).thenReturn(Optional.empty());
        when(categoryRepository.save(any())).thenReturn(saved);

        CategoryResponse response = categoryService.createCustomCategory("test@example.com", request);

        assertEquals("SideIncome", response.getName());
        assertTrue(response.isCustom());
    }

    @Test
    void createCustomCategory_DuplicateThrows() {
        CategoryRequest request = new CategoryRequest();
        request.setName("MyCustom");
        request.setType(TransactionType.EXPENSE);

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.existsByNameAndUser("MyCustom", user)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> categoryService.createCustomCategory("test@example.com", request));
    }

    @Test
    void createCustomCategory_SameAsDefaultThrows() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Salary");
        request.setType(TransactionType.INCOME);

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.existsByNameAndUser("Salary", user)).thenReturn(false);
        when(categoryRepository.findByNameAndUserIsNull("Salary")).thenReturn(Optional.of(defaultCat));

        assertThrows(ConflictException.class,
                () -> categoryService.createCustomCategory("test@example.com", request));
    }

    @Test
    void deleteCustomCategory_Success() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndUserIsNull("MyCustom")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUser("MyCustom", user)).thenReturn(Optional.of(customCat));
        when(transactionRepository.existsByCategory(customCat)).thenReturn(false);

        Map<String, String> result = categoryService.deleteCustomCategory("test@example.com", "MyCustom");

        assertEquals("Category deleted successfully", result.get("message"));
        verify(categoryRepository).delete(customCat);
    }

    @Test
    void deleteCustomCategory_DefaultThrowsForbidden() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndUserIsNull("Salary")).thenReturn(Optional.of(defaultCat));

        assertThrows(ForbiddenException.class,
                () -> categoryService.deleteCustomCategory("test@example.com", "Salary"));
    }

    @Test
    void deleteCustomCategory_UsedByTransactionThrows() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndUserIsNull("MyCustom")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUser("MyCustom", user)).thenReturn(Optional.of(customCat));
        when(transactionRepository.existsByCategory(customCat)).thenReturn(true);

        assertThrows(ValidationException.class,
                () -> categoryService.deleteCustomCategory("test@example.com", "MyCustom"));
    }

    @Test
    void deleteCustomCategory_NotFoundThrows() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameAndUserIsNull("Ghost")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUser("Ghost", user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.deleteCustomCategory("test@example.com", "Ghost"));
    }
}
