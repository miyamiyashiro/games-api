package com.meuapi.games_api.infrastructure;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "ApiKeyAuth",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-Key",
        description = "Chave de API gerada pelo endpoint POST /usuarios/{id}/api-key. " +
                      "Obrigatoria para POST, PUT, PATCH e DELETE. GETs sao publicos."
)
@OpenAPIDefinition(
        info = @Info(
                title = "GamesBoard API",
                version = "1.0.0",
                description = "Sistema completo para gestao de acervos e emprestimos de jogos.\n\n" +
                        "## Autenticacao com X-API-Key (HTTP 401)\n" +
                        "Operacoes de escrita (POST, PUT, PATCH, DELETE) exigem o header `X-API-Key`.\n" +
                        "Sem a chave ou com uma chave invalida, a API retorna **401 Unauthorized**.\n" +
                        "Fluxo: 1) Crie um usuario em `POST /usuarios` | " +
                        "2) Gere sua chave em `POST /usuarios/{id}/api-key` | " +
                        "3) Use a chave no header `X-API-Key` em todas as operacoes de escrita.\n\n" +
                        "## Rate Limiting (HTTP 429)\n" +
                        "Limite de 10 requisicoes por minuto por IP. " +
                        "Ao exceder, o IP fica bloqueado 30 segundos. " +
                        "O header `Retry-After` informa o tempo de espera.\n\n" +
                        "## Idempotencia (HTTP 409)\n" +
                        "Envie `Idempotency-Key` no header em POST/PUT/PATCH. " +
                        "Se a mesma chave for reutilizada com JSON diferente, retorna **409 Conflict**.\n\n" +
                        "## CORS\n" +
                        "A API aceita requisicoes cross-origin e libera os headers `X-API-Key` e `Idempotency-Key`. " +
                        "Tambem expoe os headers de rate limit e autenticacao para clientes web.\n\n" +
                        "## Versionamento\n" +
                        "A API demonstra versionamento por URL nos endpoints `GET /api/v1/status` e `GET /api/v2/status`.",
                contact = @Contact(name = "Luana Miyashiro")
        ),
        security = @SecurityRequirement(name = "ApiKeyAuth")
)
public class OpenApiConfig {
}
