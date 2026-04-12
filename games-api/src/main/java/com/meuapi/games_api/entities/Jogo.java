package com.meuapi.games_api.entities;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor; 
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.meuapi.games_api.entities.Plataforma;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
    private List<Plataforma> plataformas;

    public Jogo(String titulo, Categoria categoria) {
        this.titulo = titulo;
        this.categoria = categoria;
    }
}

