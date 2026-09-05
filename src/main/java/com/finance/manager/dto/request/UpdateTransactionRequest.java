package com.finance.manager.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for updating an existing transaction. The transaction date
 * cannot be changed once created; all fields here are optional.
 */
@Data
public class UpdateTransactionRequest {

    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    private String category;

    private String description;
}
