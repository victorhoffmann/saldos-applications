package com.itau.consulta.dto;

import java.math.BigDecimal;

public record BalanceDTO(
        BigDecimal amount,
        String currency
) {}
