package com.finance.manager.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.finance.manager.entity.TransactionType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {
    private String name;
    private TransactionType type;

    @JsonProperty("isCustom")
    private boolean isCustom;
}
