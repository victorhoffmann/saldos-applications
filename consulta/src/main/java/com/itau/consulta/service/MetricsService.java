package com.itau.consulta.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

    private final Counter messagesAccountConsultSucessCounter;
    private final Counter messagesAccountConsultNotFoundCounter;
    private final Counter messagesAllTransactionsConsultSucessCounter;
    private final Counter messagesAllTransactionsConsultNotFoundCounter;
    private final Counter messagesLastTransactionConsultSucessCounter;
    private final Counter messagesLastTransactionConsultNotFoundCounter;

    public MetricsService(MeterRegistry meterRegistry) {
        this.messagesAccountConsultSucessCounter = Counter.builder("account.consult.sucess")
                .description("Número de contas consultadas com sucesso")
                .register(meterRegistry);

        this.messagesAccountConsultNotFoundCounter = Counter.builder("account.consult.not_found")
                .description("Número de contas consultadas inexistentes")
                .register(meterRegistry);

        this.messagesAllTransactionsConsultSucessCounter = Counter.builder("transaction.all.sucess")
                .description("Número de transações da conta consultadas com sucesso")
                .register(meterRegistry);

        this.messagesAllTransactionsConsultNotFoundCounter = Counter.builder("transaction.all.not_found")
                .description("Número de transações da conta consultadas inexistentes")
                .register(meterRegistry);

        this.messagesLastTransactionConsultSucessCounter = Counter.builder("transaction.last.sucess")
                .description("Número de última transação da conta consultadas com sucesso")
                .register(meterRegistry);

        this.messagesLastTransactionConsultNotFoundCounter = Counter.builder("transaction.last.not_found")
                .description("Número de última transação da conta consultadas inexistentes")
                .register(meterRegistry);

    }

    public void incrementAccountConsultSucess() {
        messagesAccountConsultSucessCounter.increment();
    }

    public void incrementAccountConsultNotFound() {
        messagesAccountConsultNotFoundCounter.increment();
    }

    public void incrementAllTransactionsConsultSucess() {
        messagesAllTransactionsConsultSucessCounter.increment();
    }

    public void incrementAllTransactionsConsultNotFound() {
        messagesAllTransactionsConsultNotFoundCounter.increment();
    }

    public void incrementLastTransactionConsultSucess() {
        messagesLastTransactionConsultSucessCounter.increment();
    }

    public void incrementLastTransactionConsultNotFound() {
        messagesLastTransactionConsultNotFoundCounter.increment();
    }

}
