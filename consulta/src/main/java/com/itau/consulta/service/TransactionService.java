package com.itau.consulta.service;

import com.itau.consulta.dto.TransactionResponseDTO;
import com.itau.consulta.entity.TransactionEntity;
import com.itau.consulta.enums.TransactionFlow;
import com.itau.consulta.exceptions.TransactionNotFoundException;
import com.itau.consulta.exceptions.TransactionSystemUnavailableException;
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
    private final MetricsService metricsService;

    public List<TransactionResponseDTO> getTransactions(UUID accountId, boolean lastTransaction) {
        if(lastTransaction) return getLastTransaction(accountId, TransactionFlow.LAST_TRANSACTION);
        return getAllTransactions(accountId, TransactionFlow.ALL_TRANSACTIONS);
    }

    private List<TransactionResponseDTO> getAllTransactions(UUID accountId, TransactionFlow allTransactions) {
        log.info("Consultando transações da conta: {}", accountId);
        List<TransactionEntity> entities = transactionRepository.findAllByAccountId(accountId);
        if(isNull(entities) || entities.isEmpty()) {
            metricsService.incrementAllTransactionsConsultNotFound();
            throw new TransactionNotFoundException();
        }

        metricsService.incrementAllTransactionsConsultSucess();
        return toListTransactionResponseDTO(entities);
    }

    private List<TransactionResponseDTO> getLastTransaction(UUID accountId, TransactionFlow lastTransaction) {
        log.info("Consultando ultima transação da conta: {}", accountId);
        TransactionEntity entity = transactionRepository.findTopByAccountIdOrderByCreatedAtDesc(accountId);
        if(isNull(entity)) {
            metricsService.incrementLastTransactionConsultNotFound();
            throw new TransactionNotFoundException();
        }

        metricsService.incrementLastTransactionConsultSucess();
        return toListTransactionResponseDTO(List.of(entity));
    }

    private List<TransactionResponseDTO> toListTransactionResponseDTO(List<TransactionEntity> entities) {
        return entities.stream()
                .map(TransactionResponseDTO::toTransactionResponseDTO)
                .toList();
    }

    public List<TransactionResponseDTO> fallbackGetTransactions(UUID accountId, boolean lastTransaction, TransactionFlow flow, Exception exception) {
        if(flow.equals(TransactionFlow.LAST_TRANSACTION)) {
            metricsService.incrementLastTransactionConsultSystemUnavailable();
        } else metricsService.incrementAllTransactionsConsultSystemUnavailable();
        throw new TransactionSystemUnavailableException();
    }

}
