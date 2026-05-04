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
                        "A API implementa HATEOAS para navegabilidade, paginação em todas as listagens, " +
                        "relacionamentos One-to-One, One-to-Many e Many-to-Many, validação de dados " +
                        "e consultas personalizadas por domínio. Desenvolvido com Spring Boot 3 e banco H2.\n\n" +
                        "## Rate Limiting (HTTP 429)\n" +
                        "A API limita cada IP a 10 requisições por minuto. " +
                        "Ao exceder o limite, o acesso fica bloqueado por **30 segundos** e a API retorna HTTP 429. " +
                        "O header `Retry-After` indica quantos segundos aguardar.\n\n" +
                        "## Idempotência (HTTP 409)\n" +
                        "Para requisições POST, PUT e PATCH, envie o header `Idempotency-Key` com um UUID único por operação. " +
                        "Se a mesma chave for reutilizada com um body JSON diferente, a API retorna HTTP 409 Conflict.",
                contact = @Contact(name = "Luana Miyashiro")
        )
)
public class OpenApiConfig {
}
