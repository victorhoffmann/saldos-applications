package com.itau.consulta.dto;

import java.time.OffsetDateTime;

public record ErrorResponse(
        String message,
        OffsetDateTime timestamp,
        String path
) {}
