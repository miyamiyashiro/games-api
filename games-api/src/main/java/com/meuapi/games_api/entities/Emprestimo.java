package com.meuapi.games_api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Emprestimo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(example = "2026-04-11", description = "Data de emprestimo")
    private LocalDate dataEmprestimo;

    @Schema(example = "2026-04-20", description = "Data prevista para devolucao")
    private LocalDate dataDevolucao;

    @ManyToOne
    @JsonIgnoreProperties("emprestimos")
    @JoinColumn(name = "usuario_id")
    @NotNull(message = "O usuario e obrigatorio")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "jogo_id")
    @JsonIgnoreProperties({"editora", "plataformas", "detalhes"})
    @NotNull(message = "O jogo e obrigatorio")
    private Jogo jogo;
}
