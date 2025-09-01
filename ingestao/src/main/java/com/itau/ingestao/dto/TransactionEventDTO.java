package com.itau.ingestao.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TransactionEventDTO(
        @NotNull @Valid TransactionDTO transaction,
        @NotNull @Valid AccountDTO account
) {}
