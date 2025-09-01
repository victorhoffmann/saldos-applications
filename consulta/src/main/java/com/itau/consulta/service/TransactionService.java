package com.itau.consulta.service;

import com.itau.consulta.dto.TransactionResponseDTO;
import com.itau.consulta.entity.TransactionEntity;
import com.itau.consulta.exceptions.TransactionNotFoundException;
import com.itau.consulta.exceptions.TransactionSystemUnavailableException;
import com.itau.consulta.repository.TransactionRepository;
import io.github.resilience4j.retry.annotation.Retry;
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

    @Retry(name = "transactionServiceRetry", fallbackMethod = "fallbackGetTransactions")
    public List<TransactionResponseDTO> getTransactions(UUID accountId, boolean lastTransaction) {
        if(lastTransaction) return getLastTransaction(accountId);
        return getAllTransactions(accountId);
    }

    private List<TransactionResponseDTO> getAllTransactions(UUID accountId) {
        log.info("Consultando transações da conta: {}", accountId);
        List<TransactionEntity> entities = transactionRepository.findAllByAccountId(accountId);
        if(isNull(entities) || entities.isEmpty()) {
            metricsService.incrementAllTransactionsConsultNotFound();
            throw new TransactionNotFoundException();
        }

        metricsService.incrementAllTransactionsConsultSucess();
        return toListTransactionResponseDTO(entities);
    }

    private List<TransactionResponseDTO> getLastTransaction(UUID accountId) {
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

    public List<TransactionResponseDTO> fallbackGetTransactions(UUID accountId, boolean lastTransaction, Exception exception) {
        if (lastTransaction) {
            log.error("Falha ao recuperar última transação da conta {}: {}", accountId, exception.getMessage());
            metricsService.incrementLastTransactionConsultSystemUnavailable();
        } else {
            log.error("Falha ao recuperar transações da conta {}: {}", accountId, exception.getMessage());
            metricsService.incrementAllTransactionsConsultSystemUnavailable();
        }
        throw new TransactionSystemUnavailableException();
    }

}
