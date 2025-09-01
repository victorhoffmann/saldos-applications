package com.itau.ingestao.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BalanceDTO(
        @NotNull @DecimalMin("0.00") Double amount,
        @NotBlank @Size(min = 3, max = 3) String currency
) {}
