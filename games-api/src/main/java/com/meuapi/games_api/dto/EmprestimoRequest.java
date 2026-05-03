package com.meuapi.games_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EmprestimoRequest(
        @Schema(example = "2026-04-11", description = "Data de emprestimo. Se omitida no cadastro, a API usa a data atual.")
        LocalDate dataEmprestimo,

        @Schema(example = "2026-04-20", description = "Data prevista para devolucao")
        LocalDate dataDevolucao,

        @Schema(example = "1", description = "ID do usuario ja cadastrado")
        @NotNull(message = "O usuario e obrigatorio")
        Long usuarioId,

        @Schema(example = "1", description = "ID do jogo ja cadastrado")
        @NotNull(message = "O jogo e obrigatorio")
        Long jogoId
) {
}
