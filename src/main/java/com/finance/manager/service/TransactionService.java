package com.finance.manager.service;

import com.finance.manager.dto.request.TransactionRequest;
import com.finance.manager.dto.request.UpdateTransactionRequest;
import com.finance.manager.dto.response.TransactionResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for transaction management.
 */
public interface TransactionService {

    TransactionResponse createTransaction(String username, TransactionRequest request);

    List<TransactionResponse> getTransactions(String username, LocalDate startDate, LocalDate endDate, Long categoryId);

    TransactionResponse updateTransaction(String username, Long id, UpdateTransactionRequest request);

    Map<String, String> deleteTransaction(String username, Long id);
}
