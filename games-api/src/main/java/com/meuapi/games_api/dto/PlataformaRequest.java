package com.meuapi.games_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PlataformaRequest(
        @Schema(example = "Tabuleiro fisico", description = "Nome da plataforma de jogo")
        @NotBlank(message = "O nome da plataforma e obrigatorio")
        String nome
) {
}
