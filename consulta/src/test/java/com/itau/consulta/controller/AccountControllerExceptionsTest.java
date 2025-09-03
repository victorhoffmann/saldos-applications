package com.itau.consulta.controller;

import com.itau.consulta.exceptions.AccountSystemUnavailableException;
import com.itau.consulta.exceptions.TransactionSystemUnavailableException;
import com.itau.consulta.service.AccountService;
import com.itau.consulta.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AccountControllerExceptionsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    private UUID idConta;

    @BeforeEach
    void setUp() {
        idConta = UUID.randomUUID();
    }

    @Test
    void testServiceUnavailableAccount() throws Exception {
        when(accountService.getAccount(idConta))
                .thenThrow(new AccountSystemUnavailableException());

        mockMvc.perform(get("/accounts/{id}", idConta))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message")
                        .value("Indisponibilidade sistemica ao consultar saldo da conta"));
    }

}
