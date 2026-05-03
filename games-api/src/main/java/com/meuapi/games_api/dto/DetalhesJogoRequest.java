package com.meuapi.games_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DetalhesJogoRequest(
        @Schema(example = "Jogo de negociacao e estrategia para familias.", description = "Resumo do jogo")
        @NotBlank(message = "A descricao e obrigatoria")
        String descricao,

        @Schema(example = "10", description = "Idade minima recomendada")
        @NotNull(message = "A idade minima e obrigatoria")
        @Min(value = 0, message = "A idade minima nao pode ser negativa")
        @Max(value = 18, message = "A idade minima deve ser no maximo 18")
        Integer idadeMinima,

        @Schema(example = "90", description = "Tempo medio de partida em minutos")
        @NotNull(message = "O tempo medio e obrigatorio")
        @Min(value = 1, message = "O tempo medio deve ser maior que zero")
        Integer tempoMedioMinutos,

        @Schema(example = "1", description = "ID do jogo ja cadastrado")
        @NotNull(message = "O jogo vinculado e obrigatorio")
        Long jogoId
) {
}
