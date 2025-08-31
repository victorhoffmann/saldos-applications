package com.itau.consulta.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

    private final Counter messagesAccountConsultSucessCounter;
    private final Counter messagesAccountConsultNotFoundCounter;

    public MetricsService(MeterRegistry meterRegistry) {
        this.messagesAccountConsultSucessCounter = Counter.builder("account.consult.sucess")
                .description("Número de contas consultadas com sucesso")
                .register(meterRegistry);

        this.messagesAccountConsultNotFoundCounter = Counter.builder("account.consult.not_found")
                .description("Número de contas consultadas inexistentes")
                .register(meterRegistry);

    }

    public void incrementAccountConsultSucess() {
        messagesAccountConsultSucessCounter.increment();
    }

    public void incrementAccountConsultNotFound() {
        messagesAccountConsultNotFoundCounter.increment();
    }

}
