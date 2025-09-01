package com.itau.consulta.controller;

import com.itau.consulta.dto.AccountResponseDTO;
import com.itau.consulta.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getAccountBalance(
            @PathVariable UUID id
    ) {
        var response = accountService.getAccount(id);
        return ResponseEntity.ok(response);
    }
}
