package com.itau.consulta.controller;

import com.itau.consulta.entity.TransactionEntity;
import com.itau.consulta.repository.TransactionRepository;
import com.itau.consulta.service.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.reset;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockitoBean
    private MetricsService metricsService;

    private UUID idConta;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        reset(metricsService);
        idConta = UUID.fromString("c1bf34e0-57b0-4fa6-bc49-6909f2a1afed");
    }

    @Test
    void testAllTransactionsOK() throws Exception {
        List<TransactionEntity> entityList = List.of(returnTransactionEntity(UUID.randomUUID(), "DEBIT"), returnTransactionEntity(UUID.randomUUID(), "CREDIT"));
        transactionRepository.saveAll(entityList);

        mockMvc.perform(get("/accounts/{id}/transactions", idConta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(
                        entityList.get(0).getId().toString(),
                        entityList.get(1).getId().toString()
                )))
                .andExpect(jsonPath("$[0].account_id").value(idConta.toString()))
                .andExpect(jsonPath("$[*].type", containsInAnyOrder(
                        "DEBIT",
                        "CREDIT"
                )))
                .andExpect(jsonPath("$[0].amount").value(48.01))
                .andExpect(jsonPath("$[0].currency").value("BRL"))
                .andExpect(jsonPath("$[1].amount").value(48.01))
                .andExpect(jsonPath("$[1].currency").value("BRL"));

        verify(metricsService).incrementAllTransactionsConsultSucess();
    }

    @Test
    void testAllTransactionsNOKEmpty() throws Exception {
        mockMvc.perform(get("/accounts/{id}/transactions", idConta))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transação não encontrada"));

        verify(metricsService).incrementAllTransactionsConsultNotFound();
    }

    @Test
    void testLastTransactionsOK() throws Exception {
        TransactionEntity entity = returnTransactionEntity(UUID.randomUUID(), "DEBIT");
        transactionRepository.save(entity);

        mockMvc.perform(get("/accounts/{id}/transactions?last_transaction=true", idConta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(entity.getId().toString()))
                .andExpect(jsonPath("$[0].account_id").value(idConta.toString()))
                .andExpect(jsonPath("$[0].type").value("DEBIT"))
                .andExpect(jsonPath("$[0].amount").value(48.01))
                .andExpect(jsonPath("$[0].currency").value("BRL"));

        verify(metricsService).incrementLastTransactionConsultSucess();
    }

    @Test
    void testLastTransactionsNOKEmpty() throws Exception {
        mockMvc.perform(get("/accounts/{id}/transactions?last_transaction=true", idConta))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transação não encontrada"));

        verify(metricsService).incrementLastTransactionConsultNotFound();
    }

    @Test
    void testLastTransactionsLastTransactionNOK() throws Exception {
        mockMvc.perform(get("/accounts/{id}/transactions?last_transaction=truuue", idConta))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parâmetro 'last_transaction' deve ser true ou false"));

    }

    private TransactionEntity returnTransactionEntity(UUID transaction, String type) {
        return TransactionEntity.builder()
                .id(transaction)
                .accountId(idConta)
                .type(type)
                .amount(BigDecimal.valueOf(48.01))
                .currency("BRL")
                .status("APPROVED")
                .timestampOriginal(Instant.now().toEpochMilli())
                .createdAt(Instant.now())
                .build();
    }
}
