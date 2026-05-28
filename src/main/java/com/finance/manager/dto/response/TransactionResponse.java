package com.finance.manager.dto.response;

import com.finance.manager.entity.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String category;
    private String description;
    private TransactionType type;
}
