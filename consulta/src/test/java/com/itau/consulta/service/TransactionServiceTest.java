package com.itau.consulta.service;

import com.itau.consulta.exceptions.TransactionNotFoundException;
import com.itau.consulta.exceptions.TransactionSystemUnavailableException;
import com.itau.consulta.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @MockitoBean
    private TransactionRepository transactionRepository;

    @MockitoBean
    private MetricsService metricsService;

    private UUID idConta;

    @BeforeEach
    void setUp() {
        idConta = UUID.randomUUID();
        reset(metricsService);
    }

    @Test
    void testGetAllTransactionsEmpty() {
        when(transactionRepository.findAllByAccountId(idConta))
                .thenReturn(Collections.emptyList());

        assertThrows(TransactionNotFoundException.class, () ->
                transactionService.getTransactions(idConta, false)
        );

        verify(metricsService).incrementAllTransactionsConsultNotFound();
    }

    @Test
    void testGetAllTransactionsNull() {
        when(transactionRepository.findAllByAccountId(idConta))
                .thenReturn(null);

        assertThrows(TransactionNotFoundException.class, () ->
                transactionService.getTransactions(idConta, false)
        );

        verify(metricsService).incrementAllTransactionsConsultNotFound();
    }

    @Test
    void testGetAllTransactionsFallback() {
        when(transactionRepository.findAllByAccountId(idConta))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("Falha no Banco de Dados"));

        assertThrows(TransactionSystemUnavailableException.class, () ->
                transactionService.getTransactions(idConta, false)
        );

        verify(metricsService).incrementAllTransactionsConsultSystemUnavailable();
        verify(transactionRepository, times(4)).findAllByAccountId(idConta);
    }

    @Test
    void testGetLastTransactionFallback() {
        when(transactionRepository.findTopByAccountIdOrderByCreatedAtDesc(idConta))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("Falha no Banco de Dados"));

        assertThrows(TransactionSystemUnavailableException.class, () ->
                transactionService.getTransactions(idConta, true)
        );

        verify(metricsService).incrementLastTransactionConsultSystemUnavailable();
        verify(transactionRepository, times(4)).findTopByAccountIdOrderByCreatedAtDesc(idConta);
    }
}

