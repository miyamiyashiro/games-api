package com.meuapi.games_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditoraRequest(
        @Schema(example = "Galapagos Jogos", description = "Nome da editora")
        @NotBlank(message = "O nome e obrigatorio")
        @Size(min = 2, message = "O nome deve ter pelo menos 2 caracteres")
        String nome
) {
}
