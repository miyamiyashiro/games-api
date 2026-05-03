package com.meuapi.games_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @Schema(example = "Luana Miyashiro", description = "Nome completo do usuario")
        @NotBlank(message = "O nome e obrigatorio")
        @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres")
        String nome,

        @Schema(example = "luana@email.com", description = "E-mail de contato unico")
        @Email(message = "E-mail invalido")
        @NotBlank(message = "O e-mail e obrigatorio")
        String email
) {
}
