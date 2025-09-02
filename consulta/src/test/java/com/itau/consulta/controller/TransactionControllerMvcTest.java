package com.itau.consulta.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.itau.consulta.entity.TransactionEntity;
import com.itau.consulta.repository.TransactionRepository;
import com.itau.consulta.service.MetricsService;
import com.itau.consulta.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@WebMvcTest(TransactionController.class)
@ActiveProfiles("test")
public class TransactionControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private TransactionRepository transactionRepository;

    private UUID idConta;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TransactionRepository transactionRepository() {
            return mock(TransactionRepository.class);
        }

        @Bean
        public MetricsService metricsService() {
            return mock(MetricsService.class);
        }

        @Bean
        public TransactionService transactionService(TransactionRepository repo, MetricsService metrics) {
            return new TransactionService(repo, metrics);
        }
    }


    @BeforeEach
    void setUp() {
        idConta = UUID.fromString("c1bf34e0-57b0-4fa6-bc49-6909f2a1afed");
        Mockito.reset(metricsService);
    }

    @Test
    void testAllTransactionsOK() throws Exception {

        List<TransactionEntity> entityList = new ArrayList<TransactionEntity>();
        entityList.add(returnTransactionEntityDEBIT());
        entityList.add(returnTransactionEntityCREDIT());

        when(transactionRepository.findAllByAccountId(idConta)).thenReturn(entityList);

        mockMvc.perform(get("/accounts/{id}/transactions", idConta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(idConta.toString()))
                .andExpect(jsonPath("$[0].account_id").value(idConta.toString()))
                .andExpect(jsonPath("$[0].type").value("DEBIT"))
                .andExpect(jsonPath("$[0].amount").value(48.01))
                .andExpect(jsonPath("$[0].currency").value("BRL"))
                .andExpect(jsonPath("$[1].type").value("CREDIT"))
                .andExpect(jsonPath("$[1].amount").value(48.01))
                .andExpect(jsonPath("$[1].currency").value("BRL"));

        verify(metricsService).incrementAllTransactionsConsultSucess();

    }

    @Test
    void testAllTransactionsNOKNull() throws Exception {

        when(transactionRepository.findAllByAccountId(idConta)).thenReturn(null);

        mockMvc.perform(get("/accounts/{id}/transactions", idConta))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transação não encontrada"));


        verify(metricsService).incrementAllTransactionsConsultNotFound();

    }

    @Test
    void testAllTransactionsNOKEmpty() throws Exception {

        when(transactionRepository.findAllByAccountId(idConta)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/accounts/{id}/transactions", idConta))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transação não encontrada"));

        verify(metricsService).incrementAllTransactionsConsultNotFound();

    }

    @Test
    void testLastTransactionsOK() throws Exception {

        when(transactionRepository.findTopByAccountIdOrderByCreatedAtDesc(idConta)).thenReturn(returnTransactionEntityDEBIT());

        mockMvc.perform(get("/accounts/{id}/transactions?last_transaction=true", idConta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(idConta.toString()))
                .andExpect(jsonPath("$[0].account_id").value(idConta.toString()))
                .andExpect(jsonPath("$[0].type").value("DEBIT"))
                .andExpect(jsonPath("$[0].amount").value(48.01))
                .andExpect(jsonPath("$[0].currency").value("BRL"));

        verify(metricsService).incrementLastTransactionConsultSucess();

    }

    @Test
    void testLastTransactionsNOKNull() throws Exception {

        when(transactionRepository.findTopByAccountIdOrderByCreatedAtDesc(idConta)).thenReturn(null);

        mockMvc.perform(get("/accounts/{id}/transactions?last_transaction=true", idConta))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transação não encontrada"));


        verify(metricsService).incrementLastTransactionConsultNotFound();

    }

    @Test
    void testLastTransactionsNOKEmpty() throws Exception {

        when(transactionRepository.findAllByAccountId(idConta)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/accounts/{id}/transactions?last_transaction=true", idConta))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transação não encontrada"));

        verify(metricsService).incrementLastTransactionConsultNotFound();

    }

    @Test
    void testLastTransactionsBooleanInvalidNOK() throws Exception {

        when(transactionRepository.findAllByAccountId(idConta)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/accounts/{id}/transactions?last_transaction=aaaaaa", idConta))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parâmetro 'last_transaction' deve ser true ou false"));

    }

    private TransactionEntity returnTransactionEntityDEBIT() {
        return TransactionEntity.builder()
                .id(idConta)
                .accountId(UUID.fromString("c1bf34e0-57b0-4fa6-bc49-6909f2a1afed"))
                .type("DEBIT")
                .amount(BigDecimal.valueOf(48.01))
                .currency("BRL")
                .status("APPROVED")
                .timestampOriginal(1756819535862955L)
                .createdAt(Instant.ofEpochSecond(
                        1756819535862955L / 1_000_000,
                        (1756819535862955L % 1_000_000) * 1000
                ))
                .build();
    }

    private TransactionEntity returnTransactionEntityCREDIT() {
        return TransactionEntity.builder()
                .id(idConta)
                .accountId(UUID.fromString("c1bf34e0-57b0-4fa6-bc49-6909f2a1afed"))
                .type("CREDIT")
                .amount(BigDecimal.valueOf(48.01))
                .currency("BRL")
                .status("APPROVED")
                .timestampOriginal(1756819535862955L)
                .createdAt(Instant.ofEpochSecond(
                        1756819535862955L / 1_000_000,
                        (1756819535862955L % 1_000_000) * 1000
                ))
                .build();
    }

}
