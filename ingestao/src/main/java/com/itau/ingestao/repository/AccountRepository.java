package com.itau.ingestao.repository;

import com.itau.ingestao.entity.AccountEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO accounts (id, owner, balance_amount, balance_currency, last_transaction_timestamp)
            VALUES (:id, :owner, :balanceAmount, :balanceCurrency, :lastTransactionTimestamp)
            ON CONFLICT (id) DO NOTHING
            RETURNING 1
        """, nativeQuery = true)
    Integer insertIfAccountNotExists(@Param("id") UUID id,
                          @Param("owner") UUID owner,
                          @Param("balanceAmount") double balanceAmount,
                          @Param("balanceCurrency") String balanceCurrency,
                          @Param("lastTransactionTimestamp") long lastTransactionTimestamp);

    @Modifying
    @Transactional
    @Query(value = """
    UPDATE accounts
       SET balance_amount             = :amount,
           balance_currency           = :currency,
           last_transaction_timestamp = :timestamp,
           updated_at                 = to_timestamp(:timestamp / 1000000.0)
     WHERE id = :accountId
       AND (last_transaction_timestamp IS NULL OR last_transaction_timestamp < :timestamp)
    """, nativeQuery = true)
    int updateBalanceIfNewer(@Param("accountId") UUID accountId,
                             @Param("amount") double amount,
                             @Param("currency") String currency,
                             @Param("timestamp") long timestamp);

    // USAR NA OUTRA APP
    Optional<AccountEntity> findByOwner(UUID owner);
}
