package com.itau.ingestao.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

    private final Counter messagesProcessedCounter;
    private final Counter messagesInvalidCounter;
    private final Counter messagesErrorCounter;
    private final Counter messagesDuplicateCounter;
    private final Counter messagesAccountCreatedCounter;
    private final Counter messagesAccountBalanceUpdateCounter;
    private final Counter messagesAccountBalanceNotUpdateCounter;

    public MetricsService(MeterRegistry meterRegistry) {
        this.messagesProcessedCounter = Counter.builder("sqs.messages.processed")
                .description("Número de mensagens processadas com sucesso")
                .register(meterRegistry);

        this.messagesInvalidCounter = Counter.builder("sqs.messages.invalid")
                .description("Número de mensagens inválidas")
                .register(meterRegistry);

        this.messagesErrorCounter = Counter.builder("sqs.messages.error")
                .description("Número de mensagens com erro")
                .register(meterRegistry);

        this.messagesDuplicateCounter = Counter.builder("sqs.messages.duplicate")
                .description("Número de mensagens duplicadas")
                .register(meterRegistry);

        this.messagesAccountCreatedCounter = Counter.builder("account.created")
                .description("Número de contas criadas")
                .register(meterRegistry);

        this.messagesAccountBalanceUpdateCounter = Counter.builder("account.balance.update")
                .description("Número de saldos atualizados")
                .register(meterRegistry);

        this.messagesAccountBalanceNotUpdateCounter = Counter.builder("account.balance.notupdate")
                .description("Número de saldos não atualizados")
                .register(meterRegistry);
    }

    public void incrementProcessed() {
        messagesProcessedCounter.increment();
    }

    public void incrementInvalid() {
        messagesInvalidCounter.increment();
    }

    public void incrementError() {
        messagesErrorCounter.increment();
    }

    public void incrementDuplicate() {
        messagesDuplicateCounter.increment();
    }

    public void incrementAccountCreated() {
        messagesAccountCreatedCounter.increment();
    }

    public void incrementAccountBalanceUpdate() {
        messagesAccountBalanceUpdateCounter.increment();
    }

    public void incrementAccountBalanceNotUpdate() {
        messagesAccountBalanceNotUpdateCounter.increment();
    }
}
