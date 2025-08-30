package com.itau.ingestao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID owner;

    @Column(name = "balance_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAmount;

    @Column(name = "balance_currency", length = 3, nullable = false)
    private String balanceCurrency;

    @Column(name = "last_transaction_timestamp")
    private Long lastTransactionTimestamp;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
