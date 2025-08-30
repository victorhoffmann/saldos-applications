package com.itau.ingestao.repository;

import com.itau.ingestao.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    boolean existsById(UUID id);

    // Opcional: buscar última transação por conta (para consistência) <- USAR NA OUTRA APP
    TransactionEntity findTopByAccountIdOrderByCreatedAtDesc(UUID accountId);

}
