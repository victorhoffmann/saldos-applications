package com.itau.ingestao.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.ingestao.dto.TransactionEventDTO;
import com.itau.ingestao.service.AccountService;
import com.itau.ingestao.service.MetricsService;
import com.itau.ingestao.service.TransactionService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsConsumer {

    private final SqsClient sqsClient;
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final MetricsService metricsService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    @Value("${aws.sqs.max-messages}")
    private Integer maxNumberMessages;

    @Value("${aws.sqs.wait-time}")
    private Integer waitTime;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @PostConstruct
    public void startConsumeSQS() {
        pollSqs();
    }

    private void pollSqs() {
        CompletableFuture.runAsync(() -> {
            try {
                var receiveRequest = ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(maxNumberMessages)
                        .waitTimeSeconds(waitTime)
                        .build();

                List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

                if (!messages.isEmpty()) {
                    messages.forEach(msg -> executor.submit(() -> processMessage(msg)));
                }

            } catch (Exception e) {
                log.error("Erro no polling do SQS: {}", e.getMessage(), e);
            } finally {
                pollSqs();
            }
        });
    }

    private void processMessage(Message msg) {

        try {
            var event = objectMapper.readValue(msg.body(), TransactionEventDTO.class);
            log.info("Processando transação: {}", event.transaction().id());

            Set<ConstraintViolation<TransactionEventDTO>> violations = validator.validate(event);
            if (!violations.isEmpty()) {
                log.error("Mensagem inválida: {}", violations);
                metricsService.incrementInvalid();
                deleteMessage(msg);
                return;
            }

            if (transactionService.existsTransaction(event.transaction().id())) {
                log.info("Transação já processada: {}", event.transaction().id());
                metricsService.incrementDuplicate();
                deleteMessage(msg);
                return;
            }

            accountService.createAccountIfNotExists(event);
            accountService.updateBalance(event);

            transactionService.insert(event);

            log.info("Transação processada com sucesso: {}", event.transaction().id());
            metricsService.incrementProcessed();
            deleteMessage(msg);

        } catch (JsonProcessingException jsonException) {
            log.error("Erro ao desserializar mensagem: {}", jsonException.getLocalizedMessage());
            metricsService.incrementInvalid();
            deleteMessage(msg);

        } catch (Exception exception) {
            log.error("Erro ao processar mensagem: {}", exception.getMessage());
            metricsService.incrementError();

            // Poderia implementar um retry com base no número de tentativas e por fim encaminhar para uma fila DLQ
        }
    }

    private void deleteMessage(Message msg) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(msg.receiptHandle())
                .build());
    }
}
