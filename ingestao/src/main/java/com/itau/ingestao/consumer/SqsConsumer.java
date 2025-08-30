package com.itau.ingestao.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.ingestao.dto.TransactionEventDTO;
import com.itau.ingestao.service.AccountService;
import com.itau.ingestao.service.MetricsService;
import com.itau.ingestao.service.TransactionService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.Set;

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

    @Scheduled(fixedDelay = 5000)
    public void consumeMessages() {
        var messages = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .build()
        ).messages();

        for (var msg : messages) {
            try {

                var event = objectMapper.readValue(msg.body(), TransactionEventDTO.class);

                Set<ConstraintViolation<TransactionEventDTO>> violations = validator.validate(event);
                if (!violations.isEmpty()) {
                    log.error("Mensagem inválida: {}", violations);
                    metricsService.incrementInvalid();
                    deleteMessage(msg);
                    continue;
                }

                if (transactionService.existsTransaction(event.getTransaction().getId())) {
                    log.info("Transação já processada: {}", event.getTransaction().getId());
                    metricsService.incrementDuplicate();
                    deleteMessage(msg);
                    continue;
                }

                accountService.createAccountIfNotExists(event);
                accountService.updateBalance(event);

                transactionService.insert(event);

                log.info("Transação processada com sucesso: {}", event.getTransaction().getId());
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
    }

    private void deleteMessage(Message msg) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(msg.receiptHandle())
                .build());
    }
}
