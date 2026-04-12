package com.meuapi.games_api.entities;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@Entity
@Data
public class Editora {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(example = "Galápagos Jogos", description = "Nome da editora")
    @NotBlank @Size(min = 2)
    private String nome;

    @JsonIgnoreProperties("editora")
    @OneToMany(mappedBy = "editora")
    private List<Jogo> jogos;
}

