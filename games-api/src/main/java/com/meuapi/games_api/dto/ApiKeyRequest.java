package com.meuapi.games_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ApiKeyRequest(
        @NotNull(message = "O ID do usuario e obrigatorio")
        @Positive(message = "O ID do usuario deve ser positivo")
        Long usuarioId
) {
}
