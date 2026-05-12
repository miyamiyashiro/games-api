package com.meuapi.games_api.infrastructure;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "ApiKeyAuth",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-Key",
        description = "Chave de API gerada pelo endpoint POST /usuarios/{id}/api-key"
)
@OpenAPIDefinition(
        info = @Info(
                title = "GamesBoard API",
                version = "1.0.0",
                description = "Sistema completo para gestao de acervos e emprestimos de jogos. " +
                        "A API implementa HATEOAS para navegabilidade, paginacao em todas as listagens, " +
                        "relacionamentos One-to-One, One-to-Many e Many-to-Many, validacao de dados " +
                        "e consultas personalizadas por dominio. Desenvolvido com Spring Boot 3 e banco H2.\n\n" +
                        "## Autenticacao com Chave de API (HTTP 401)\n" +
                        "Operacoes sensiveis de escrita exigem o header X-API-Key. " +
                        "A chave pode ser gerada ou renovada em POST /usuarios/{id}/api-key.\n\n" +
                        "## Rate Limiting (HTTP 429)\n" +
                        "A API limita cada IP a 10 requisicoes por minuto. " +
                        "Ao exceder o limite, o acesso fica bloqueado por 30 segundos e a API retorna HTTP 429. " +
                        "As respostas incluem os headers X-RateLimit-Limit, X-RateLimit-Remaining e X-RateLimit-Reset. " +
                        "Enquanto o cliente estiver bloqueado, o header Retry-After informa quantos segundos aguardar.\n\n" +
                        "## Idempotencia (HTTP 409)\n" +
                        "Para requisicoes POST, PUT e PATCH, envie o header Idempotency-Key com um UUID unico por operacao. " +
                        "Se a mesma chave for reutilizada no mesmo endpoint com o mesmo JSON, a API ignora o reprocessamento. " +
                        "Se o JSON, o metodo ou o endpoint forem alterados para a mesma chave, a API retorna HTTP 409 Conflict.",
                contact = @Contact(name = "Luana Miyashiro")
        )
)
public class OpenApiConfig {
}
