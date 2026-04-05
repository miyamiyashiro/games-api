package com.meuapi.games_api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Acervo Nerdola API",
                version = "1.0.0",
                description = "API para gestão de acervo e empréstimos de jogos de tabuleiro e RPG.",
                contact = @Contact(name = "Luana Miyashiro", email = "luakawaii13@gmail.com")
        )
)

public class OpenApiConfig {
}
