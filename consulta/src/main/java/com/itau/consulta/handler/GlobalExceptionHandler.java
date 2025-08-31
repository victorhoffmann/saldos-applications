package com.itau.consulta.handler;

import com.itau.consulta.exceptions.AccountNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(
            String message,
            OffsetDateTime timestamp,
            String path
    ) {}

    private static OffsetDateTime nowSaoPaulo() {
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"));
        return now.withNano((now.getNano() / 1_000_000) * 1_000_000);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException exception,
                                                               HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "Conta não encontrada",
                        nowSaoPaulo(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUUID(MethodArgumentTypeMismatchException ex,
                                                           HttpServletRequest request) {
        if (ex.getRequiredType() != null && ex.getRequiredType().equals(UUID.class)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "ID informado não é um UUID válido",
                            nowSaoPaulo(),
                            request.getRequestURI()
                    ));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        ex.getMessage(),
                        nowSaoPaulo(),
                        request.getRequestURI()
                ));
    }
}
