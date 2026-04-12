package com.meuapi.games_api.entities;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Data

public class Emprestimo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(example = "2026-04-11", description = "Data de empréstimo")
    @NotNull
    private LocalDate dataEmprestimo;

    @Schema(example = "2026-04-20", description = "Data prevista para devolução")
    private LocalDate dataDevolucao;

    @ManyToOne
    @JsonIgnoreProperties("emprestimos")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "jogo_id")
    @JsonIgnoreProperties("editora")
    private Jogo jogo;
}
