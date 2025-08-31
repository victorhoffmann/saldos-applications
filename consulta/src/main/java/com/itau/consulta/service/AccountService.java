package com.itau.consulta.service;

import com.itau.consulta.dto.AccountResponseDTO;
import com.itau.consulta.dto.BalanceDTO;
import com.itau.consulta.entity.AccountEntity;
import com.itau.consulta.exceptions.AccountNotFoundException;
import com.itau.consulta.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountResponseDTO getAccount(UUID accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        return AccountResponseDTO
                .builder()
                    .id(account.getId())
                    .owner(account.getOwner())
                    .balance(BalanceDTO
                            .builder()
                            .amount(account.getBalanceAmount())
                            .currency(account.getBalanceCurrency())
                            .build())
                    .updatedAt(account.getUpdatedAt())
                .build();
    }
}
