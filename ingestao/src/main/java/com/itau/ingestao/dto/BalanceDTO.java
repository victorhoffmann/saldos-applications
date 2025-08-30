package com.itau.ingestao.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BalanceDTO {

    @NotNull
    @DecimalMin("0.00")
    private double amount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;
}
