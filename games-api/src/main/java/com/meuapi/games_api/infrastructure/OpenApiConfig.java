package com.meuapi.games_api.infrastructure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

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
                        "## Parte I - Fundamentos REST\n" +
                        "- Projeto Maven com Spring Boot, JPA/Hibernate e H2.\n" +
                        "- CRUD REST para Jogos, Usuarios, Editoras, Plataformas, Emprestimos e Detalhes dos Jogos.\n" +
                        "- Respostas paginadas com Spring Data Pageable e links HATEOAS.\n" +
                        "- Relacionamentos JPA: One-to-One, One-to-Many, Many-to-One e Many-to-Many.\n" +
                        "- Enum Categoria para classificar os jogos.\n" +
                        "- Validacao com Bean Validation nos DTOs de entrada.\n" +
                        "- Consultas personalizadas, tratamento global de erros, README, colecao Postman e deploy.\n\n" +
                        "## Parte II - Autenticacao com X-API-Key (HTTP 401)\n" +
                        "Operacoes de escrita (POST, PUT, PATCH, DELETE) exigem o header `X-API-Key`.\n" +
                        "Consultas GET, `POST /usuarios` e `POST /usuarios/{id}/api-key` sao publicos para permitir o fluxo inicial.\n" +
                        "Sem a chave ou com uma chave invalida, a API retorna **401 Unauthorized**.\n" +
                        "Fluxo: 1) Crie um usuario em `POST /usuarios` | " +
                        "2) Gere sua chave em `POST /usuarios/{id}/api-key` | " +
                        "3) Use a chave no header `X-API-Key` em todas as operacoes de escrita.\n\n" +
                        "## Parte II - Rate Limiting (HTTP 429)\n" +
                        "Limite de 10 requisicoes por minuto por IP. " +
                        "Ao exceder, o IP fica bloqueado 30 segundos. " +
                        "O header `Retry-After` informa o tempo de espera.\n\n" +
                        "## Parte II - Idempotencia (HTTP 409)\n" +
                        "Envie `Idempotency-Key` no header em POST/PUT/PATCH. " +
                        "Se a mesma chave for reutilizada com JSON diferente, retorna **409 Conflict**.\n\n" +
                        "## Parte II - CORS\n" +
                        "A API aceita requisicoes cross-origin e libera os headers `X-API-Key` e `Idempotency-Key`. " +
                        "Tambem expoe os headers de rate limit e autenticacao para clientes web.\n\n" +
                        "## Parte II - Versionamento\n" +
                        "A API demonstra versionamento por URL em dois contratos do recurso Jogos: " +
                        "`GET /api/v1/jogos` retorna uma versao simplificada e " +
                        "`GET /api/v2/jogos` retorna uma versao completa com HATEOAS. " +
                        "Tambem existem os endpoints auxiliares `GET /api/v1/status` e `GET /api/v2/status`.\n\n" +
                        "## Tratamento de erros\n" +
                        "Erros seguem o contrato `ApiErrorResponse`, com `timestamp`, `status`, `erro`, " +
                        "`mensagem`, `caminho`, `metodo` e `detalhes`.",
                contact = @Contact(name = "Luana Miyashiro")
        ),
        tags = {
                @Tag(name = "Jogos", description = "Recurso central do acervo. Demonstra enum, paginacao, HATEOAS, Many-to-One com Editora e Many-to-Many com Plataformas."),
                @Tag(name = "Usuarios", description = "Clientes que podem realizar emprestimos. Tambem fornece o fluxo publico para gerar X-API-Key."),
                @Tag(name = "Editoras", description = "Editoras dos jogos. Demonstra One-to-Many com Jogos e consulta personalizada por nome."),
                @Tag(name = "Plataformas", description = "Plataformas ou formatos dos jogos. Demonstra Many-to-Many com Jogos."),
                @Tag(name = "Emprestimos", description = "Controle de emprestimos, relacionando Usuario e Jogo com validacao de datas."),
                @Tag(name = "Detalhes dos Jogos", description = "Informacoes complementares em relacionamento One-to-One com Jogo."),
                @Tag(name = "Versionamento", description = "Endpoints auxiliares de status em v1 e v2."),
                @Tag(name = "Jogos Versionados", description = "Contratos v1 e v2 do recurso Jogos, demonstrando evolucao de resposta por URL.")
        }
)
public class OpenApiConfig {

    private static final Set<PathItem.HttpMethod> PROTECTED_METHODS = Set.of(
            PathItem.HttpMethod.POST,
            PathItem.HttpMethod.PUT,
            PathItem.HttpMethod.PATCH,
            PathItem.HttpMethod.DELETE
    );

    @Bean
    OpenApiCustomizer apiKeySecurityOnlyForProtectedWrites() {
        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }
            openApi.getComponents().addSchemas("ApiErrorResponse", apiErrorResponseSchema());

            openApi.getPaths().forEach((path, pathItem) ->
                    pathItem.readOperationsMap().forEach((method, operation) -> {
                        boolean protectedWrite = PROTECTED_METHODS.contains(method) && !isPublicBootstrapRoute(method, path);

                        addResponseIfAbsent(operation, "400", "Requisicao invalida, JSON mal formatado ou dados fora do contrato.");
                        addResponseIfAbsent(operation, "429", "Muitas requisicoes. O cliente deve aguardar o tempo indicado em Retry-After.");

                        if (protectedWrite) {
                            operation.addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"));
                            addResponseIfAbsent(operation, "401", "Chave de API ausente ou invalida.");
                        } else {
                            operation.getResponses().remove("401");
                        }

                        if (method == PathItem.HttpMethod.POST
                                || method == PathItem.HttpMethod.PUT
                                || method == PathItem.HttpMethod.PATCH) {
                            addResponseIfAbsent(operation, "409", "Conflito de idempotencia ou regra de unicidade.");
                        }
                    })
            );
        };
    }

    private boolean isPublicBootstrapRoute(PathItem.HttpMethod method, String path) {
        return method == PathItem.HttpMethod.POST
                && ("/usuarios".equals(path) || "/usuarios/{id}/api-key".equals(path));
    }

    private void addResponseIfAbsent(Operation operation, String code, String description) {
        if (!operation.getResponses().containsKey(code)) {
            operation.getResponses().addApiResponse(code, new ApiResponse()
                    .description(description)
                    .content(errorContent()));
        }
    }

    private Content errorContent() {
        return new Content().addMediaType("application/json",
                new io.swagger.v3.oas.models.media.MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ApiErrorResponse")));
    }

    private Schema<?> apiErrorResponseSchema() {
        return new ObjectSchema()
                .description("Resposta padronizada para erros da API")
                .addProperty("timestamp", new StringSchema().example("2026-05-21T18:39:00"))
                .addProperty("status", new IntegerSchema().example(404))
                .addProperty("erro", new StringSchema().example("Not Found"))
                .addProperty("mensagem", new StringSchema().example("Endpoint nao encontrado"))
                .addProperty("caminho", new StringSchema().example("/jogos/999"))
                .addProperty("metodo", new StringSchema().example("GET"))
                .addProperty("detalhes", new ArraySchema().items(new StringSchema())
                        .example(List.of("Confira o caminho da URL e consulte /swagger-ui/index.html.")));
    }
}
