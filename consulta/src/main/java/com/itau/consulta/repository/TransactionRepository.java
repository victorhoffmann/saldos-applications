package com.itau.consulta.repository;

import com.itau.consulta.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    TransactionEntity findTopByAccountIdOrderByCreatedAtDesc(UUID accountId);
    List<TransactionEntity> findAllByAccountId(UUID accountId);
}
