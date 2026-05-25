package com.meuapi.games_api.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meuapi.games_api.exceptions.ApiErrorResponse;
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
import java.util.Set;

@Component
@Order(2)
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";
    private static final String AUTHENTICATE_HEADER = "WWW-Authenticate";
    private static final Set<String> METODOS_PROTEGIDOS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    public ApiKeyFilter(UsuarioRepository usuarioRepository, ObjectMapper objectMapper) {
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!deveAutenticar(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.isBlank()) {
            escreverRespostaNaoAutorizada(request, response, "Header X-API-Key nao informado.");
            return;
        }

        if (usuarioRepository.findByApiKey(apiKey).isEmpty()) {
            escreverRespostaNaoAutorizada(request, response, "Chave de API invalida ou inexistente.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean deveAutenticar(HttpServletRequest request) {
        String metodo = request.getMethod().toUpperCase();
        String path = request.getRequestURI();

        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console")) {
            return false;
        }

        if (path.startsWith("/api-keys")) {
            return !("POST".equals(metodo) && path.equals("/api-keys"));
        }

        if (!METODOS_PROTEGIDOS.contains(metodo)) {
            return false;
        }

        if ("POST".equals(metodo) && path.equals("/usuarios")) {
            return false;
        }

        return true;
    }

    private void escreverRespostaNaoAutorizada(
            HttpServletRequest request,
            HttpServletResponse response,
            String detalhe
    ) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(AUTHENTICATE_HEADER, "ApiKey realm=\"Games API\"");

        ApiErrorResponse corpo = new ApiErrorResponse(
                LocalDateTime.now().toString(),
                401,
                "Unauthorized",
                "Acesso negado: informe uma chave de API valida no header X-API-Key.",
                request.getRequestURI(),
                request.getMethod(),
                List.of(detalhe, "Gere sua chave em: POST /api-keys")
        );

        objectMapper.writeValue(response.getWriter(), corpo);
    }
}
