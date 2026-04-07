package com.meuapi.games_api.entities;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List; 
@Entity
@Data
public class Editora {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 2)
    private String nome;

    @OneToMany(mappedBy = "editora")
    private List<Jogo> jogos;
}

