package com.itau.consulta.controller;

import com.itau.consulta.dto.TransactionResponseDTO;
import com.itau.consulta.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionResponseDTO>> getAccountTransactions(
            @PathVariable UUID id,
            @RequestParam(name = "last_transaction", required = false) Boolean lastTransaction
    ) {
        var response = transactionService.getTransactions(id, Boolean.TRUE.equals(lastTransaction));
        return ResponseEntity.ok(response);
    }
}
