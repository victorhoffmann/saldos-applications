package com.itau.consulta.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.itau.consulta.entity.AccountEntity;
import com.itau.consulta.repository.AccountRepository;
import com.itau.consulta.service.AccountService;
import com.itau.consulta.service.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@WebMvcTest(AccountController.class)
@ActiveProfiles("test")
public class AccountControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private AccountRepository accountRepository;

    private UUID idConta;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AccountRepository accountRepository() {
            return mock(AccountRepository.class);
        }

        @Bean
        public MetricsService metricsService() {
            return mock(MetricsService.class);
        }

        @Bean
        public AccountService accountService(AccountRepository repo, MetricsService metrics) {
            return new AccountService(repo, metrics);
        }
    }


    @BeforeEach
    void setUp() {
        idConta = UUID.fromString("c1bf34e0-57b0-4fa6-bc49-6909f2a1afed");
    }

    @Test
    void testConsultaContaOK() throws Exception {

        AccountEntity entity = returnContaEntityOK();

        when(accountRepository.findById(idConta)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/accounts/{id}", idConta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idConta.toString()))
                .andExpect(jsonPath("$.balance.amount").value(48.01))
                .andExpect(jsonPath("$.balance.currency").value("BRL"));

        verify(metricsService).incrementAccountConsultSucess();

    }

    @Test
    void testConsultaContaNOK() throws Exception {

        when(accountRepository.findById(idConta)).thenReturn(Optional.empty());

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

    @Test
    void testConsultaContaErroInesperado() throws Exception {
        when(accountRepository.findById(idConta)).thenThrow(new RuntimeException("Conexão falhou"));

        mockMvc.perform(get("/accounts/{id}", idConta))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado. Por favor, tente novamente mais tarde"));

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
