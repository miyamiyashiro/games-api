package com.meuapi.games_api.entities;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor; // Adicione este import
import lombok.Data;
import lombok.NoArgsConstructor; // Adicione este import

@Entity
@Data
@NoArgsConstructor  // Cria o construtor vazio que o banco de dados exige
@AllArgsConstructor // Cria o construtor com todos os campos que o LoadDatabase precisa
public class Jogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título é obrigatório")
    private String titulo;

    @ManyToOne
    @JoinColumn(name = "editora_id")
    private Editora editora;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "A categoria é obrigatória")
    private Categoria categoria;

    // Se o seu LoadDatabase usa apenas titulo e categoria,
    // adicione este construtor manual para não dar erro:
    public Jogo(String titulo, Categoria categoria) {
        this.titulo = titulo;
        this.categoria = categoria;
    }
}

