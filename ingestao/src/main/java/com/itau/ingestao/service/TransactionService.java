package com.itau.ingestao.service;

import com.itau.ingestao.dto.TransactionEventDTO;
import com.itau.ingestao.entity.TransactionEntity;
import com.itau.ingestao.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public boolean existsTransaction (String id) {
        return transactionRepository.existsById(UUID.fromString(id));
    }


    public void insert(TransactionEventDTO event) {
        var transactionEntity = TransactionEntity.fromDTO(event);
        transactionRepository.save(transactionEntity);
    }
}
