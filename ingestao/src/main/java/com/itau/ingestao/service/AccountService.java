package com.itau.ingestao.service;

import com.itau.ingestao.dto.TransactionEventDTO;
import com.itau.ingestao.repository.AccountRepository;
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

    public void createAccountIfNotExists(TransactionEventDTO eventDTO) {
        var result = accountRepository.insertIfAccountNotExists(
                UUID.fromString(eventDTO.getAccount().getId()),
                UUID.fromString(eventDTO.getAccount().getOwner()),
                eventDTO.getAccount().getBalance().getAmount(),
                eventDTO.getAccount().getBalance().getCurrency(),
                0L
        );

        if (result != null) {
            log.info("Conta criada com sucesso: {}", result);
            metricsService.incrementAccountCreated();
        }
    }

    public void updateBalance(TransactionEventDTO event) {
        int result = accountRepository.updateBalanceIfNewer(
                UUID.fromString(event.getAccount().getId()),
                event.getTransaction().getAmount(),
                event.getTransaction().getCurrency(),
                event.getTransaction().getTimestamp()
        );

        if (result == 1) {
            log.info("Saldo do cliente atualizado: {}, transação: {}", event.getAccount().getOwner(), event.getTransaction().getId());
            metricsService.incrementAccountBalanceUpdate();
        } else {
            log.info("Evento antigo saldo do cliente não foi atualizado: {}, transação: {}", event.getAccount().getOwner(), event.getTransaction().getId());
            metricsService.incrementAccountBalanceNotUpdate();
        }
    }
}
