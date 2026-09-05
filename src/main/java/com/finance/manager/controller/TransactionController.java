package com.finance.manager.controller;

import com.finance.manager.dto.request.TransactionRequest;
import com.finance.manager.dto.request.UpdateTransactionRequest;
import com.finance.manager.dto.response.TransactionResponse;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for creating, listing (with optional filters), updating,
 * and deleting the current user's transactions.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Creates a new transaction.
     *
     * @return 201 Created with the new transaction, or 400 Bad Request for
     *         a future-dated or otherwise invalid transaction
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists the current user's transactions, newest first. All query
     * parameters are optional and may be combined freely.
     *
     * @param startDate  inclusive lower bound on transaction date
     * @param endDate    inclusive upper bound on transaction date
     * @param categoryId restrict to a single category, by its id from {@code GET /api/categories}
     * @param type       restrict to INCOME or EXPENSE transactions
     * @return 200 OK with the matching transactions
     */
    @GetMapping
    public ResponseEntity<Map<String, List<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TransactionType type) {
        List<TransactionResponse> transactions = transactionService.getTransactions(
                userDetails.getUsername(), startDate, endDate, categoryId, type);
        return ResponseEntity.ok(Map.of("transactions", transactions));
    }

    /**
     * Updates an existing transaction. The transaction's date cannot be changed.
     *
     * @return 200 OK with the updated transaction, or 404 Not Found if it doesn't exist for this user
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        TransactionResponse response = transactionService.updateTransaction(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a transaction. Deleted transactions are excluded from all
     * future savings goal progress calculations and reports.
     *
     * @return 200 OK on success, or 404 Not Found if it doesn't exist for this user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Map<String, String> response = transactionService.deleteTransaction(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }
}
