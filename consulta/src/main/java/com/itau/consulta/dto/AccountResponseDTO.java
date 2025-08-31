package com.itau.consulta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountResponseDTO(
        UUID id,
        UUID owner,
        BalanceDTO balance,
        @JsonProperty("updated_at") OffsetDateTime updatedAt
) {}
