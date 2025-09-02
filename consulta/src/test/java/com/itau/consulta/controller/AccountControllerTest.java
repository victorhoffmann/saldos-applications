package com.itau.consulta.controller;

import com.itau.consulta.entity.AccountEntity;
import com.itau.consulta.repository.AccountRepository;
import com.itau.consulta.service.AccountService;
import com.itau.consulta.service.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class AccountControllerTest {

    private AccountController accountController;
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MetricsService metricsService;

    private UUID idConta;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, metricsService);
        accountController = new AccountController(accountService);
        idConta = UUID.fromString("c1bf34e0-57b0-4fa6-bc49-6909f2a1afed");
    }

    @Test
    void testConsultaContaOK() throws Exception {

        when(accountRepository.findById(idConta)).thenReturn(Optional.ofNullable(returnContaEntityOK()));
        accountController.getAccountBalance(idConta);

    }

    private AccountEntity returnContaEntityOK() {
        return AccountEntity.builder()
                .id(idConta)
                .owner(UUID.fromString("47bb9808-74b1-4d28-9306-6ce2436f7309"))
                .balanceAmount(BigDecimal.valueOf(48.01))
                .balanceCurrency("BRL")
                .lastTransactionTimestamp(1756819535862955L)
                .updatedAt(Instant.now())
                .build();
    }

}
