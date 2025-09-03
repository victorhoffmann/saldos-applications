package com.itau.consulta.controller;

import com.itau.consulta.exceptions.TransactionSystemUnavailableException;
import com.itau.consulta.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TransactionControllerExceptionsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    private UUID idConta;

    @BeforeEach
    void setUp() {
        idConta = UUID.randomUUID();
    }

    @Test
    void testServiceUnavailableTransaction() throws Exception {
        when(transactionService.getTransactions(idConta, false))
                .thenThrow(new TransactionSystemUnavailableException());

        mockMvc.perform(get("/accounts/{id}/transactions", idConta))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message")
                        .value("Indisponibilidade sistemica ao consultar transação da conta"));
    }

    @Test
    void testGenericExceptionHandler() throws Exception {
        when(transactionService.getTransactions(idConta, false))
                .thenThrow(new RuntimeException("Erro inesperado"));

        mockMvc.perform(get("/accounts/{id}/transactions", idConta))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message")
                        .value("Ocorreu um erro inesperado. Por favor, tente novamente mais tarde"));
    }

}
