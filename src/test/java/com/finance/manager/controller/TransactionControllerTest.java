package com.finance.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.manager.dto.request.TransactionRequest;
import com.finance.manager.dto.response.TransactionResponse;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.service.TransactionService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(com.finance.manager.config.SecurityConfig.class)
class TransactionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private TransactionService transactionService;
    @MockBean private com.finance.manager.security.CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "user@example.com")
    void createTransaction_Success() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("1000"));
        request.setDate(LocalDate.now().minusDays(1));
        request.setCategory("Salary");

        TransactionResponse response = TransactionResponse.builder()
                .id(1L).amount(new BigDecimal("1000")).date(LocalDate.now().minusDays(1))
                .category("Salary").type(TransactionType.INCOME).build();

        when(transactionService.createTransaction(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("Salary"))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getTransactions_Success() throws Exception {
        TransactionResponse t = TransactionResponse.builder()
                .id(1L).amount(new BigDecimal("500")).date(LocalDate.now())
                .category("Food").type(TransactionType.EXPENSE).build();

        when(transactionService.getTransactions(any(), any(), any(), any())).thenReturn(List.of(t));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].category").value("Food"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void deleteTransaction_NotFound_Returns404() throws Exception {
        when(transactionService.deleteTransaction(any(), eq(99L)))
                .thenThrow(new ResourceNotFoundException("Transaction not found: 99"));

        mockMvc.perform(delete("/api/transactions/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactions_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void deleteTransaction_Success() throws Exception {
        when(transactionService.deleteTransaction(any(), eq(1L)))
                .thenReturn(Map.of("message", "Transaction deleted successfully"));

        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));
    }
}
