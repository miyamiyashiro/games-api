package com.meuapi.games_api.dto;

import com.meuapi.games_api.entities.Categoria;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Schema(description = "Resposta completa da versao 2 do recurso Jogo")
public class JogoV2Response extends RepresentationModel<JogoV2Response> {

    @Schema(example = "1")
    private final Long id;

    @Schema(example = "Catan")
    private final String titulo;

    @Schema(example = "TABULEIRO")
    private final Categoria categoria;

    @Schema(example = "Galapagos Jogos")
    private final String editora;

    @Schema(example = "[\"Tabuleiro fisico\"]")
    private final List<String> plataformas;

    @Schema(example = "Jogo de negociacao e estrategia.")
    private final String descricao;

    @Schema(example = "10")
    private final Integer idadeMinima;

    @Schema(example = "90")
    private final Integer tempoMedioMinutos;

    public JogoV2Response(
            Long id,
            String titulo,
            Categoria categoria,
            String editora,
            List<String> plataformas,
            String descricao,
            Integer idadeMinima,
            Integer tempoMedioMinutos
    ) {
        this.id = id;
        this.titulo = titulo;
        this.categoria = categoria;
        this.editora = editora;
        this.plataformas = plataformas;
        this.descricao = descricao;
        this.idadeMinima = idadeMinima;
        this.tempoMedioMinutos = tempoMedioMinutos;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getEditora() {
        return editora;
    }

    public List<String> getPlataformas() {
        return plataformas;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getIdadeMinima() {
        return idadeMinima;
    }

    public Integer getTempoMedioMinutos() {
        return tempoMedioMinutos;
    }
}
