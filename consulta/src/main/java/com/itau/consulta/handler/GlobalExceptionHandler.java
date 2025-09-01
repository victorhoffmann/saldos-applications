package com.itau.consulta.handler;

import com.itau.consulta.exceptions.AccountNotFoundException;
import com.itau.consulta.exceptions.TransactionNotFoundException;
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

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotFound(TransactionNotFoundException exception,
                                                               HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "Transação não encontrada",
                        nowSaoPaulo(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUUID(MethodArgumentTypeMismatchException exception,
                                                           HttpServletRequest request) {
        if (exception.getRequiredType() == UUID.class) {
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
                        "Erro de tipo inesperado ao processar a requisição",
                        nowSaoPaulo(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleExceptionGeneric(Exception exception,
                                                                HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde",
                        nowSaoPaulo(),
                        request.getRequestURI()
                ));
    }
}
