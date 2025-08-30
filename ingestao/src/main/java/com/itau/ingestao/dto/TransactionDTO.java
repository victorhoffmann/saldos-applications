package com.itau.ingestao.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TransactionDTO {

    @NotNull
    private String id;

    @NotBlank
    private String type;

    @NotNull
    @DecimalMin("0.01")
    private Double amount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    @NotBlank
    private String status;

    @NotNull
    private Long timestamp;
}