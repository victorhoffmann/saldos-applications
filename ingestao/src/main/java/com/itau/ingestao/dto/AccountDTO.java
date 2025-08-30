package com.itau.ingestao.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountDTO {

    @NotNull
    private String id;

    @NotNull
    private String owner;

    @NotNull
    private long created_at;

    @NotBlank
    private String status;

    @NotNull
    @Valid
    private BalanceDTO balance;
}