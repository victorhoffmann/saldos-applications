package com.itau.consulta.controller;

import com.itau.consulta.entity.AccountEntity;
import com.itau.consulta.repository.AccountRepository;
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
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockitoBean
    private MetricsService metricsService;

    private UUID idConta;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        idConta = UUID.fromString("c1bf34e0-57b0-4fa6-bc49-6909f2a1afed");
    }

    @Test
    void testConsultaContaOK() throws Exception {
        AccountEntity entity = returnContaEntityOK();
        accountRepository.save(entity);

        mockMvc.perform(get("/accounts/{id}", idConta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idConta.toString()))
                .andExpect(jsonPath("$.balance.amount").value(48.01))
                .andExpect(jsonPath("$.balance.currency").value("BRL"));

        verify(metricsService).incrementAccountConsultSucess();
    }

    @Test
    void testConsultaContaNOK() throws Exception {
        mockMvc.perform(get("/accounts/{id}", idConta))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Conta não encontrada"));

        verify(metricsService).incrementAccountConsultNotFound();
    }

    @Test
    void testConsultaContaUUIDInvalidNOK() throws Exception {
        mockMvc.perform(get("/accounts/{id}", "aaaaaaaaaa"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID informado não é um UUID válido"));
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
