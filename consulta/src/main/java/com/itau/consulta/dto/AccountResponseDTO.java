package com.itau.consulta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDTO {

    private UUID id;

    private UUID owner;

    private BalanceDTO balance;

    @JsonProperty("updated_at")
    private Instant updatedAt;
}
