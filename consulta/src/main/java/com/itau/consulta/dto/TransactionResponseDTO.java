package com.itau.consulta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.itau.consulta.entity.TransactionEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

public record TransactionResponseDTO(
        UUID id,
        @JsonProperty("account_id") UUID accountId,
        String type,
        BigDecimal amount,
        String currency,
        String status,
        @JsonProperty("timestamp") Long timestampOriginal,
        @JsonProperty("created_at") OffsetDateTime createdAt
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
