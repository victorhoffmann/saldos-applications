package com.itau.ingestao.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransactionDTO(
        @NotNull String id,
        @NotBlank String type,
        @NotNull @DecimalMin("0.01") Double amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotBlank String status,
        @NotNull Long timestamp
) {}
