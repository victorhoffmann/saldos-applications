package com.itau.consulta.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class AccountSystemUnavailableException extends RuntimeException {
    public AccountSystemUnavailableException() {super();}
}
