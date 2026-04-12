package com.meuapi.games_api.infrastructure;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "GamesBoard API",
                version = "1.0.0",
                description = "Sistema completo para gestão de acervos e empréstimos de jogos. " +
                        "A API implementa **HATEOAS** para navegabilidade, **Paginação** em todas as listagens, " +
                        "relacionamentos complexos (incluindo **Many-to-Many**) e consultas personalizadas por domínio. " +
                        "Desenvolvido com Spring Boot 3 e persistência em banco H2.",
                contact = @Contact(name = "Luana Miyashiro")
        )
)

public class OpenApiConfig {
}
