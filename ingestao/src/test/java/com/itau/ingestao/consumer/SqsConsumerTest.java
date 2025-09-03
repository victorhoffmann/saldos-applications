package com.itau.ingestao.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.ingestao.dto.TransactionEventDTO;
import com.itau.ingestao.entity.TransactionEntity;
import com.itau.ingestao.repository.AccountRepository;
import com.itau.ingestao.repository.TransactionRepository;
import com.itau.ingestao.service.AccountService;
import com.itau.ingestao.service.MetricsService;
import com.itau.ingestao.service.TransactionService;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SqsConsumerTest {

    private SqsConsumer sqsConsumer;
    private AccountService accountService;
    private TransactionService transactionService;
    private ObjectMapper objectMapper;
    private Validator validator;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MetricsService metricsService;

    @Mock
    private Acknowledgement acknowledgement;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        transactionService = new TransactionService(transactionRepository, metricsService);
        accountService = new AccountService(accountRepository, metricsService);

        sqsConsumer = new SqsConsumer(transactionService, accountService, metricsService, objectMapper, validator);
    }

    @Test
    void testConsumeMessagesContaCriadaFluxoOK() throws Exception {

        String message = new String(
                getClass().getClassLoader()
                        .getResourceAsStream("messages/transaction-event-ok.json")
                        .readAllBytes()
        );

        TransactionEventDTO dto = objectMapper.readValue(message, TransactionEventDTO.class);

        UUID transactionId = UUID.fromString(dto.transaction().id());
        UUID accountId = UUID.fromString(dto.account().id());

        when(transactionRepository.existsById(transactionId)).thenReturn(false);
        when(accountRepository.insertIfAccountNotExists(accountId, UUID.fromString(dto.account().owner()), dto.account().balance().amount(), dto.account().balance().currency(), 0L)).thenReturn(UUID.randomUUID());
        when(accountRepository.updateBalanceIfNewer(accountId, dto.account().balance().amount(), dto.account().balance().currency(), dto.transaction().timestamp())).thenReturn(1);
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sqsConsumer.consumeMessages(message, acknowledgement);
        verify(metricsService).incrementAccountCreated();
        verify(metricsService).incrementAccountBalanceUpdate();
        verify(metricsService).incrementProcessed();
        verify(acknowledgement).acknowledge();

    }

    @Test
    void testConsumeMessagesEventInvalid() throws Exception {

        String message = new String(
                getClass().getClassLoader()
                        .getResourceAsStream("messages/transaction-event-invalid.json")
                        .readAllBytes()
        );

        sqsConsumer.consumeMessages(message, acknowledgement);
        verify(metricsService).incrementInvalid();
        verify(acknowledgement).acknowledge();

    }

    @Test
    void testConsumeMessagesTransacaoProcessada() throws Exception {

        String message = new String(
                getClass().getClassLoader()
                        .getResourceAsStream("messages/transaction-event-processada.json")
                        .readAllBytes()
        );

        TransactionEventDTO dto = objectMapper.readValue(message, TransactionEventDTO.class);

        UUID transactionId = UUID.fromString(dto.transaction().id());
        when(transactionRepository.existsById(transactionId)).thenReturn(true);

        sqsConsumer.consumeMessages(message, acknowledgement);
        verify(metricsService).incrementDuplicate();
        verify(acknowledgement).acknowledge();

    }

    @Test
    void testConsumeMessagesContaJaCriadaSaldoAntigoFluxoOK() throws Exception {

        String message = new String(
                getClass().getClassLoader()
                        .getResourceAsStream("messages/transaction-event-ok.json")
                        .readAllBytes()
        );

        TransactionEventDTO dto = objectMapper.readValue(message, TransactionEventDTO.class);

        UUID transactionId = UUID.fromString(dto.transaction().id());
        UUID accountId = UUID.fromString(dto.account().id());

        when(transactionRepository.existsById(transactionId)).thenReturn(false);
        when(accountRepository.insertIfAccountNotExists(accountId, UUID.fromString(dto.account().owner()), dto.account().balance().amount(), dto.account().balance().currency(), 0L)).thenReturn(null);
        when(accountRepository.updateBalanceIfNewer(accountId, dto.account().balance().amount(), dto.account().balance().currency(), dto.transaction().timestamp())).thenReturn(0);
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sqsConsumer.consumeMessages(message, acknowledgement);
        verify(metricsService).incrementAccountBalanceNotUpdate();
        verify(metricsService).incrementProcessed();
        verify(acknowledgement).acknowledge();

    }

    @Test
    void testConsumeMessagesJsonProcessingException() throws Exception {
        String message = "{ \"transaction\": { \"id\": \"123\" ";

        sqsConsumer.consumeMessages(message, acknowledgement);

        verify(metricsService).incrementInvalid();
        verify(acknowledgement).acknowledge();
    }

    @Test
    void testConsumeMessagesGenericException() throws Exception {
        String message = new String(
                getClass().getClassLoader()
                        .getResourceAsStream("messages/transaction-event-ok.json")
                        .readAllBytes()
        );

        TransactionEventDTO dto = objectMapper.readValue(message, TransactionEventDTO.class);
        UUID transactionId = UUID.fromString(dto.transaction().id());

        doThrow(new RuntimeException("Erro simulado")).when(transactionRepository).existsById(transactionId);

        sqsConsumer.consumeMessages(message, acknowledgement);

        verify(metricsService).incrementError();
    }
}