package com.meuapi.games_api.exceptions;

import java.util.List;

public record ApiErrorResponse(
        String timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        String metodo,
        List<String> detalhes
) {
}
