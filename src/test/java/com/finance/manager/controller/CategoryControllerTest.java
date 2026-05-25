package com.finance.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.CategoryResponse;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(com.finance.manager.config.SecurityConfig.class)
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CategoryService categoryService;
    @MockBean private com.finance.manager.security.CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "user@example.com")
    void getAllCategories_Success() throws Exception {
        CategoryResponse salary = CategoryResponse.builder()
                .id(1L).name("Salary").type(TransactionType.INCOME).isCustom(false).build();

        when(categoryService.getAllCategories(any())).thenReturn(List.of(salary));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].name").value("Salary"))
                .andExpect(jsonPath("$.categories[0].isCustom").value(false));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createCustomCategory_Success() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("SideIncome");
        request.setType(TransactionType.INCOME);

        CategoryResponse response = CategoryResponse.builder()
                .id(10L).name("SideIncome").type(TransactionType.INCOME).isCustom(true).build();

        when(categoryService.createCustomCategory(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("SideIncome"))
                .andExpect(jsonPath("$.isCustom").value(true));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void deleteCustomCategory_Success() throws Exception {
        when(categoryService.deleteCustomCategory(any(), any()))
                .thenReturn(Map.of("message", "Category deleted successfully"));

        mockMvc.perform(delete("/api/categories/SideIncome"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }

    @Test
    void getAllCategories_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }
}
