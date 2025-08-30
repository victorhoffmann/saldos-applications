package com.itau.ingestao.dto;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Data
public class TransactionEventDTO {

    @NotNull
    @Valid
    private TransactionDTO transaction;

    @NotNull
    @Valid
    private AccountDTO account;
}