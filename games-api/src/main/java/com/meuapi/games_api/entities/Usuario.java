package com.meuapi.games_api.entities;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(example = "Luana Miyashiro", description = "Nome completo do usuário")
    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @Schema(example = "luana@email.com", description = "E-mail de contato único")
    @Email(message = "E-mail inválido")
    @NotBlank
    private String email;

    @JsonIgnoreProperties("usuario")
    @OneToMany(mappedBy = "usuario")
    private List<Emprestimo> emprestimos;
}
