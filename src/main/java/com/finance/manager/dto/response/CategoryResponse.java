package com.finance.manager.dto.response;

import com.finance.manager.entity.TransactionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Response body for category data.
 */
@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private TransactionType type;
    @JsonProperty("isCustom")
    private boolean isCustom;
}
