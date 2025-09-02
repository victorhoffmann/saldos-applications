package com.itau.ingestao.dto;

import com.itau.ingestao.validator.ValidUUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountDTO(
        @NotNull @ValidUUID String id,
        @NotNull @ValidUUID String owner,
        @NotNull Long created_at,
        @NotBlank String status,
        @NotNull @Valid BalanceDTO balance
) {}
