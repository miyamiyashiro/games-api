package com.meuapi.games_api.dto;

import com.meuapi.games_api.entities.Categoria;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record JogoRequest(
        @Schema(example = "Catan", description = "Titulo do jogo")
        @NotBlank(message = "O titulo e obrigatorio")
        String titulo,

        @Schema(example = "TABULEIRO", description = "Categoria do jogo")
        @NotNull(message = "A categoria e obrigatoria")
        Categoria categoria,

        @Schema(example = "1", description = "ID da editora ja cadastrada")
        @NotNull(message = "A editora e obrigatoria")
        Long editoraId,

        @Schema(example = "[1]", description = "IDs das plataformas ja cadastradas")
        @NotEmpty(message = "Informe pelo menos uma plataforma")
        List<Long> plataformaIds
) {
}
