package com.itau.ingestao.service;

import com.itau.ingestao.dto.TransactionEventDTO;
import com.itau.ingestao.repository.AccountRepository;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final MetricsService metricsService;

    @Retry(name = "accountServiceRetry", fallbackMethod = "fallbackCreateAccount")
    public void createAccountIfNotExists(TransactionEventDTO event) {
        var result = accountRepository.insertIfAccountNotExists(
                UUID.fromString(event.account().id()),
                UUID.fromString(event.account().owner()),
                event.account().balance().amount(),
                event.account().balance().currency(),
                0L
        );

        if (result != null) {
            log.info("Conta criada com sucesso: {}", result);
            metricsService.incrementAccountCreated();
        }
    }

    @Retry(name = "accountServiceRetry", fallbackMethod = "fallbackUpdateBalance")
    public void updateBalance(TransactionEventDTO event) {
        int result = accountRepository.updateBalanceIfNewer(
                UUID.fromString(event.account().id()),
                event.transaction().amount(),
                event.transaction().currency(),
                event.transaction().timestamp()
        );

        if (result == 1) {
            log.info("Saldo do cliente atualizado: {}, transação: {}", event.account().owner(), event.transaction().id());
            metricsService.incrementAccountBalanceUpdate();
        } else {
            log.info("Evento antigo saldo do cliente não foi atualizado: {}, transação: {}", event.account().owner(), event.transaction().id());
            metricsService.incrementAccountBalanceNotUpdate();
        }
    }

    public void fallbackCreateAccount(TransactionEventDTO event, Exception exception) {
        metricsService.incrementAccountCreatedSystemUnavailable();
        throw new RuntimeException("Falha ao criar conta", exception);
    }

    public void fallbackUpdateBalance(TransactionEventDTO event, Exception exception) {
        metricsService.incrementAccountBalanceSystemUnavailable();
        throw new RuntimeException("Falha ao atualizar saldo", exception);
    }
}
