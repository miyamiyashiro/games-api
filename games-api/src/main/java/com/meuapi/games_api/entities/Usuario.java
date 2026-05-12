package com.meuapi.games_api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(example = "Luana Miyashiro", description = "Nome completo do usuario")
    @NotBlank(message = "O nome e obrigatorio")
    @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres")
    private String nome;

    @Schema(example = "luana@email.com", description = "E-mail de contato unico")
    @Column(unique = true)
    @Email(message = "E-mail invalido")
    @NotBlank(message = "O e-mail e obrigatorio")
    private String email;

    @JsonIgnore
    @Column(unique = true)
    private String apiKey;

    @JsonIgnoreProperties("usuario")
    @OneToMany(mappedBy = "usuario")
    private List<Emprestimo> emprestimos;
}
