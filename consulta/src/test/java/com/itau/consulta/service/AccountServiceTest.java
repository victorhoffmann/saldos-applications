package com.itau.consulta.service;

import com.itau.consulta.exceptions.AccountSystemUnavailableException;
import com.itau.consulta.exceptions.TransactionSystemUnavailableException;
import com.itau.consulta.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private MetricsService metricsService;

    private UUID idConta;

    @BeforeEach
    void setUp() {
        idConta = UUID.randomUUID();
        reset(metricsService);
    }

    @Test
    void testGetAccountFallback() {
        when(accountRepository.findById(idConta))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("Falha no Banco de Dados"));

        assertThrows(AccountSystemUnavailableException.class, () ->
                accountService.getAccount(idConta)
        );

        verify(metricsService).incrementAccountConsultSystemUnavailable();
        verify(accountRepository, times(4)).findById(idConta);
    }

}
