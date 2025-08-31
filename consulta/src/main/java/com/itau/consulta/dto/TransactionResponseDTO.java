package com.itau.consulta.dto;

import com.itau.consulta.entity.TransactionEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

public record TransactionResponseDTO(
        UUID id,
        UUID accountId,
        String type,
        BigDecimal amount,
        String currency,
        String status,
        Long timestampOriginal,
        OffsetDateTime createdAt
) {
    public static TransactionResponseDTO toTransactionResponseDTO(TransactionEntity entity) {
        OffsetDateTime createdAt = entity.getCreatedAt()
                .atZone(ZoneId.of("America/Sao_Paulo"))
                .toOffsetDateTime()
                .withNano((entity.getCreatedAt()
                        .atZone(ZoneId.of("America/Sao_Paulo"))
                        .getNano() / 1_000_000) * 1_000_000);

        return new TransactionResponseDTO(
                entity.getId(),
                entity.getAccountId(),
                entity.getType(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getTimestampOriginal(),
                createdAt
        );
    }
}
