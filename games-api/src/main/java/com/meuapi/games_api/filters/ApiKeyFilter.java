package com.meuapi.games_api.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meuapi.games_api.repositories.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Filtro de autenticação por API Key (X-API-Key).
 *
 * Endpoints protegidos: POST, PUT, DELETE em qualquer rota,
 * exceto criação de usuário e geração de chave.
 * Retorna 401 Unauthorized se a chave estiver ausente ou inválida.
 */
@Component
@Order(2)
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";
    private static final String AUTHENTICATE_HEADER = "WWW-Authenticate";

    // Métodos HTTP que exigem autenticação
    private static final Set<String> METODOS_PROTEGIDOS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    public ApiKeyFilter(UsuarioRepository usuarioRepository, ObjectMapper objectMapper) {
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!deveAutenticar(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);

        // Chave ausente ou em branco → 401
        if (apiKey == null || apiKey.isBlank()) {
            escreverRespostaNaoAutorizada(response, "Header X-API-Key nao informado.");
            return;
        }

        // Chave não encontrada no banco → 401
        if (usuarioRepository.findByApiKey(apiKey).isEmpty()) {
            escreverRespostaNaoAutorizada(response, "Chave de API invalida ou inexistente.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean deveAutenticar(HttpServletRequest request) {
        String metodo = request.getMethod().toUpperCase();
        String path = request.getRequestURI();

        // Apenas métodos de escrita são protegidos
        if (!METODOS_PROTEGIDOS.contains(metodo)) {
            return false;
        }

        // Rotas do Swagger e H2 console ficam livres
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console")) {
            return false;
        }

        // POST /usuarios → criar usuário não requer chave (bootstrap)
        if ("POST".equals(metodo) && path.equals("/usuarios")) {
            return false;
        }

        // POST /usuarios/{id}/api-key → gerar chave não requer chave
        if ("POST".equals(metodo) && path.matches("/usuarios/\\d+/api-key")) {
            return false;
        }

        return true;
    }

    private void escreverRespostaNaoAutorizada(HttpServletResponse response, String detalhe) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(AUTHENTICATE_HEADER, "ApiKey realm=\"Games API\"");

        Map<String, Object> corpo = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 401,
                "erro", "Unauthorized",
                "mensagem", "Acesso negado: informe uma chave de API valida no header X-API-Key.",
                "detalhes", List.of(detalhe, "Gere sua chave em: POST /usuarios/{id}/api-key")
        );

        objectMapper.writeValue(response.getWriter(), corpo);
    }
}
