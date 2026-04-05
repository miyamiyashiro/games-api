package com.meuapi.games_api.entities;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
public class Jogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título é obrigatório") // Bean Validation
    private String titulo;

    @ManyToOne
    @JoinColumn(name = "editora_id")
    private Editora editora; // Relacionamento Many-to-One

    @Enumerated(EnumType.STRING) // Isso faz o banco salvar o nome "RPG" em vez de um número
    @NotNull(message = "A categoria é obrigatória")
    private Categoria categoria;
}

