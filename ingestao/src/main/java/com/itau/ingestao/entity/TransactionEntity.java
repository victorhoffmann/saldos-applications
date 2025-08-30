package com.itau.ingestao.entity;

import com.itau.ingestao.dto.TransactionEventDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(nullable = false, length = 10)
    private String type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 12)
    private String status;

    @Column(name = "timestamp_original", nullable = false)
    private Long timestampOriginal;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static TransactionEntity fromDTO(TransactionEventDTO event) {
        long timestampsMicroseconds = event.getTransaction().getTimestamp();
        Instant createdAt = Instant.ofEpochSecond(timestampsMicroseconds / 1_000_000, (timestampsMicroseconds % 1_000_000) * 1000);

        return TransactionEntity.builder()
                .id(UUID.fromString(event.getTransaction().getId()))
                .accountId(UUID.fromString(event.getAccount().getId()))
                .type(event.getTransaction().getType())
                .amount(BigDecimal.valueOf(event.getTransaction().getAmount()))
                .currency(event.getTransaction().getCurrency())
                .status(event.getTransaction().getStatus())
                .timestampOriginal(timestampsMicroseconds)
                .createdAt(createdAt)
                .build();
    }
}
