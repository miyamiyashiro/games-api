package com.meuapi.games_api.dto;

import com.meuapi.games_api.entities.Categoria;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta simplificada da versao 1 do recurso Jogo")
public record JogoV1Response(
        @Schema(example = "1")
        Long id,

        @Schema(example = "Catan")
        String titulo,

        @Schema(example = "TABULEIRO")
        Categoria categoria
) {
}
