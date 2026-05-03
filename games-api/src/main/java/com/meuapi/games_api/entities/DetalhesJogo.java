package com.meuapi.games_api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Schema(description = "Detalhes complementares de um jogo do acervo")
public class DetalhesJogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A descricao e obrigatoria")
    @Schema(example = "Jogo de negociacao e estrategia para familias.", description = "Resumo do jogo")
    private String descricao;

    @NotNull(message = "A idade minima e obrigatoria")
    @Min(value = 0, message = "A idade minima nao pode ser negativa")
    @Max(value = 18, message = "A idade minima deve ser no maximo 18")
    @Schema(example = "10", description = "Idade minima recomendada")
    private Integer idadeMinima;

    @NotNull(message = "O tempo medio e obrigatorio")
    @Min(value = 1, message = "O tempo medio deve ser maior que zero")
    @Schema(example = "90", description = "Tempo medio de partida em minutos")
    private Integer tempoMedioMinutos;

    @OneToOne
    @JoinColumn(name = "jogo_id", unique = true)
    @JsonIgnoreProperties({"detalhes", "editora", "plataformas"})
    @NotNull(message = "O jogo vinculado e obrigatorio")
    @Schema(description = "Jogo ao qual os detalhes pertencem")
    private Jogo jogo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getIdadeMinima() {
        return idadeMinima;
    }

    public void setIdadeMinima(Integer idadeMinima) {
        this.idadeMinima = idadeMinima;
    }

    public Integer getTempoMedioMinutos() {
        return tempoMedioMinutos;
    }

    public void setTempoMedioMinutos(Integer tempoMedioMinutos) {
        this.tempoMedioMinutos = tempoMedioMinutos;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
    }
}
