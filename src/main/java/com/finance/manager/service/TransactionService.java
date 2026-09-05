package com.finance.manager.service;

import com.finance.manager.dto.request.TransactionRequest;
import com.finance.manager.dto.request.UpdateTransactionRequest;
import com.finance.manager.dto.response.TransactionResponse;
import com.finance.manager.entity.TransactionType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Manages financial transactions: creation, filtered retrieval, updates,
 * and deletion, all scoped to the current user.
 */
public interface TransactionService {

    /**
     * Creates a new transaction.
     *
     * @param username the current user's username
     * @param request  the amount, date (must not be in the future), category, and optional description
     * @return the created transaction
     * @throws com.finance.manager.exception.ValidationException if the date is in the future
     * @throws com.finance.manager.exception.ResourceNotFoundException if the category does not exist for this user
     */
    TransactionResponse createTransaction(String username, TransactionRequest request);

    /**
     * Retrieves the user's transactions, newest first, optionally filtered
     * by any combination of date range, category, and type.
     *
     * @param username   the current user's username
     * @param startDate  inclusive lower bound on transaction date, or {@code null} for no lower bound
     * @param endDate    inclusive upper bound on transaction date, or {@code null} for no upper bound
     * @param categoryId restrict to this category id, or {@code null} for all categories
     * @param type       restrict to this transaction type (INCOME/EXPENSE), or {@code null} for both
     * @return the matching transactions, most recent first
     */
    List<TransactionResponse> getTransactions(String username, LocalDate startDate, LocalDate endDate, Long categoryId, TransactionType type);

    /**
     * Updates an existing transaction. The transaction's date cannot be changed.
     *
     * @param username the current user's username
     * @param id       the transaction id
     * @param request  the fields to update; {@code null} fields are left unchanged
     * @return the updated transaction
     * @throws com.finance.manager.exception.ResourceNotFoundException if no such transaction exists for this user
     */
    TransactionResponse updateTransaction(String username, Long id, UpdateTransactionRequest request);

    /**
     * Deletes a transaction. Deleted transactions are excluded from all
     * future savings goal progress calculations and reports.
     *
     * @param username the current user's username
     * @param id       the transaction id
     * @return a response map containing a confirmation message
     * @throws com.finance.manager.exception.ResourceNotFoundException if no such transaction exists for this user
     */
    Map<String, String> deleteTransaction(String username, Long id);
}
