package com.meuapi.games_api.dto;

public record ApiKeyResponse(
        Long id,
        Long usuarioId,
        String usuarioNome,
        String usuarioEmail,
        String apiKey,
        String status
) {
}
