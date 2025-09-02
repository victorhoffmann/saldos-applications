package com.itau.ingestao.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.ingestao.dto.TransactionEventDTO;
import com.itau.ingestao.service.AccountService;
import com.itau.ingestao.service.MetricsService;
import com.itau.ingestao.service.TransactionService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsConsumer {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final MetricsService metricsService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @SqsListener(
            value = "${aws.sqs.queue-name}",
            maxConcurrentMessages = "${aws.sqs.listener.concurrency}",
            maxMessagesPerPoll = "${aws.sqs.listener.max-number-of-messages}",
            pollTimeoutSeconds = "${aws.sqs.listener.wait-time-seconds}",
            messageVisibilitySeconds = "${aws.sqs.listener.visibility-timeout-seconds}",
            acknowledgementMode = "MANUAL"
    )
    public void consumeMessages(@Payload String message, Acknowledgement acknowledgement) {
        try {
            var event = objectMapper.readValue(message, TransactionEventDTO.class);
            log.info("Processando transação: {}", event.transaction().id());

            Set<ConstraintViolation<TransactionEventDTO>> violations = validator.validate(event);
            if (!violations.isEmpty()) {
                log.error("Mensagem inválida: {}", violations);
                metricsService.incrementInvalid();
                acknowledgement.acknowledge();
                return;
            }

            if (transactionService.existsTransaction(event.transaction().id())) {
                log.info("Transação já processada: {}", event.transaction().id());
                metricsService.incrementDuplicate();
                acknowledgement.acknowledge();
                return;
            }

            accountService.createAccountIfNotExists(event);
            accountService.updateBalance(event);

            transactionService.insert(event);

            log.info("Transação processada com sucesso: {}", event.transaction().id());
            metricsService.incrementProcessed();
            acknowledgement.acknowledge();

        } catch (JsonProcessingException jsonException) {
            log.error("Erro ao desserializar mensagem: {}", jsonException.getLocalizedMessage());
            metricsService.incrementInvalid();
            acknowledgement.acknowledge();

        } catch (Exception exception) {
            log.error("Erro ao processar mensagem: {}", exception.getMessage());
            metricsService.incrementError();
        }
    }
}
