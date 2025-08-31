package com.itau.consulta.service;

import com.itau.consulta.dto.TransactionResponseDTO;
import com.itau.consulta.entity.TransactionEntity;
import com.itau.consulta.exceptions.TransactionNotFoundException;
import com.itau.consulta.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<TransactionResponseDTO> getTransactions(UUID accountId, boolean lastTransaction) {
        if(lastTransaction) return getLastTransaction(accountId);
        return getAllTransactions(accountId);
    }

    private List<TransactionResponseDTO> getAllTransactions(UUID accountId) {
        log.info("Consultando transações da conta: {}", accountId);
        List<TransactionEntity> entities = transactionRepository.findAllByAccountId(accountId);
        if(isNull(entities) || entities.isEmpty()) throw new TransactionNotFoundException();
        return toListTransactionResponseDTO(entities);
    }

    private List<TransactionResponseDTO> getLastTransaction(UUID accountId) {
        log.info("Consultando ultima transação da conta: {}", accountId);
        TransactionEntity entity = transactionRepository.findTopByAccountIdOrderByCreatedAtDesc(accountId);
        if(isNull(entity)) throw new TransactionNotFoundException();
        return toListTransactionResponseDTO(List.of(entity));
    }

    private List<TransactionResponseDTO> toListTransactionResponseDTO(List<TransactionEntity> entities) {
        return entities.stream()
                .map(TransactionResponseDTO::toTransactionResponseDTO)
                .toList();
    }

}
