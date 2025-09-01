package com.itau.ingestao.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountDTO(
        @NotNull String id,
        @NotNull String owner,
        @NotNull Long created_at,
        @NotBlank String status,
        @NotNull @Valid BalanceDTO balance
) {}
