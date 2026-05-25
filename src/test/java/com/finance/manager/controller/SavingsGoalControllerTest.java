package com.finance.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.finance.manager.dto.request.SavingsGoalRequest;
import com.finance.manager.dto.response.SavingsGoalResponse;
import com.finance.manager.service.SavingsGoalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SavingsGoalController.class)
@Import(com.finance.manager.config.SecurityConfig.class)
class SavingsGoalControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private SavingsGoalService savingsGoalService;
    @MockBean private com.finance.manager.security.CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "user@example.com")
    void createGoal_Success() throws Exception {
        SavingsGoalRequest request = new SavingsGoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(new BigDecimal("10000"));
        request.setTargetDate(LocalDate.now().plusYears(1));

        SavingsGoalResponse response = SavingsGoalResponse.builder()
                .id(1L).goalName("Emergency Fund").targetAmount(new BigDecimal("10000"))
                .targetDate(LocalDate.now().plusYears(1)).startDate(LocalDate.now())
                .currentProgress(BigDecimal.ZERO).progressPercentage(0.0)
                .remainingAmount(new BigDecimal("10000")).build();

        when(savingsGoalService.createGoal(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"))
                .andExpect(jsonPath("$.progressPercentage").value(0.0));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getAllGoals_Success() throws Exception {
        SavingsGoalResponse g = SavingsGoalResponse.builder()
                .id(1L).goalName("Vacation").targetAmount(new BigDecimal("5000"))
                .targetDate(LocalDate.now().plusMonths(6)).startDate(LocalDate.now())
                .currentProgress(new BigDecimal("1000")).progressPercentage(20.0)
                .remainingAmount(new BigDecimal("4000")).build();

        when(savingsGoalService.getAllGoals(any())).thenReturn(List.of(g));

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals[0].goalName").value("Vacation"))
                .andExpect(jsonPath("$.goals[0].progressPercentage").value(20.0));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void deleteGoal_Success() throws Exception {
        when(savingsGoalService.deleteGoal(any(), any()))
                .thenReturn(Map.of("message", "Goal deleted successfully"));

        mockMvc.perform(delete("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goal deleted successfully"));
    }

    @Test
    void getAllGoals_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isUnauthorized());
    }
}
