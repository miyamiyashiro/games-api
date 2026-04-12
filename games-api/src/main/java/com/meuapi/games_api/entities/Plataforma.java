package com.meuapi.games_api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Plataforma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da plataforma é obrigatório")
    @Schema(example = "PlayStation 5", description = "Nome da plataforma de jogos")
    private String nome;

    @ManyToMany(mappedBy = "plataformas")
    @JsonIgnoreProperties("plataformas")
    private List<Jogo> jogos;
}
