package com.itau.consulta.service;

import com.itau.consulta.dto.AccountResponseDTO;
import com.itau.consulta.dto.BalanceDTO;
import com.itau.consulta.exceptions.AccountNotFoundException;
import com.itau.consulta.exceptions.AccountSystemUnavailableException;
import com.itau.consulta.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import io.github.resilience4j.retry.annotation.Retry;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final MetricsService metricsService;

    @Retry(name = "accountServiceRetry", fallbackMethod = "fallbackGetAccount")
    public AccountResponseDTO getAccount(UUID accountId) {
        log.info("Consultando saldo da conta: {}", accountId);
        return accountRepository.findById(accountId)
                .map(account -> {
                    metricsService.incrementAccountConsultSucess();

                    OffsetDateTime updatedAt = account.getUpdatedAt()
                            .atZone(ZoneId.of("America/Sao_Paulo"))
                            .toOffsetDateTime()
                            .withNano((account.getUpdatedAt().atZone(ZoneId.of("America/Sao_Paulo")).getNano() / 1_000_000) * 1_000_000);

                    return new AccountResponseDTO(
                            account.getId(),
                            account.getOwner(),
                            new BalanceDTO(account.getBalanceAmount(), account.getBalanceCurrency()),
                            updatedAt
                    );
                })
                .orElseThrow(() -> {
                    log.error("Conta não encontrada: {}", accountId);
                    metricsService.incrementAccountConsultNotFound();
                    return new AccountNotFoundException();
                });
    }

    public AccountResponseDTO fallbackGetAccount(UUID accountId, Exception exception) {
        if (exception instanceof AccountNotFoundException) {
            throw (AccountNotFoundException) exception;
        }
        metricsService.incrementAccountConsultSystemUnavailable();
        throw new AccountSystemUnavailableException();
    }
}
