package com.meuapi.games_api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(example = "Catan", description = "Nome do jogo de tabuleiro")
    @NotBlank(message = "O título é obrigatório")
    private String titulo;

    @ManyToOne
    @JoinColumn(name = "editora_id")
    @JsonIgnoreProperties("jogos")
    @NotNull(message = "A editora é obrigatória")
    private Editora editora;

    @Schema(example = "TABULEIRO", description = "Categoria do jogo")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "A categoria é obrigatória")
    private Categoria categoria;

    @ManyToMany
    @JoinTable(
            name = "jogo_plataforma",
            joinColumns = @JoinColumn(name = "jogo_id"),
            inverseJoinColumns = @JoinColumn(name = "plataforma_id")
    )
    @JsonIgnoreProperties("jogos")
    @NotEmpty(message = "Informe pelo menos uma plataforma")
    private List<Plataforma> plataformas;

    @OneToOne(mappedBy = "jogo", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("jogo")
    private DetalhesJogo detalhes;

    public Jogo(String titulo, Categoria categoria) {
        this.titulo = titulo;
        this.categoria = categoria;
    }
}
