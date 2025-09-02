package com.itau.ingestao.service;

import com.itau.ingestao.dto.TransactionEventDTO;
import com.itau.ingestao.entity.TransactionEntity;
import com.itau.ingestao.repository.TransactionRepository;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final MetricsService metricsService;

    @Retry(name = "transactionServiceRetry", fallbackMethod = "fallbackExistsTransaction")
    public boolean existsTransaction (String id) {
        return transactionRepository.existsById(UUID.fromString(id));
    }

    @Retry(name = "transactionServiceRetry", fallbackMethod  = "fallbackInsertTransaction")
    public void insert(TransactionEventDTO event) {
        var transactionEntity = TransactionEntity.fromDTO(event);
        transactionRepository.save(transactionEntity);
    }

    public boolean fallbackExistsTransaction(String id, Exception exception) {
        metricsService.incrementTransactionExistsSystemUnavailable();
        throw new RuntimeException("Falha ao verificar transação", exception);
    }

    public void fallbackInsertTransaction(TransactionEventDTO event, Exception exception) {
        metricsService.incrementTransactionInsertSystemUnavailable();
        throw new RuntimeException("Falha ao salvar transação", exception);
    }
}
