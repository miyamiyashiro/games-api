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

@Component
@Order(2)
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";

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

        if (apiKey == null || apiKey.isBlank() || usuarioRepository.findByApiKey(apiKey).isEmpty()) {
            escreverRespostaNaoAutorizada(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean deveAutenticar(HttpServletRequest request) {
        String metodo = request.getMethod();
        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(metodo) || "GET".equalsIgnoreCase(metodo)) {
            return false;
        }

        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console")) {
            return false;
        }

        if ("POST".equalsIgnoreCase(metodo) && "/usuarios".equals(path)) {
            return false;
        }

        return !("POST".equalsIgnoreCase(metodo) && path.matches("/usuarios/\\d+/api-key"));
    }

    private void escreverRespostaNaoAutorizada(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> corpo = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 401,
                "erro", "Unauthorized",
                "mensagem", "Informe uma chave de API valida no header X-API-Key.",
                "detalhes", List.of("Header obrigatorio: " + API_KEY_HEADER)
        );

        objectMapper.writeValue(response.getWriter(), corpo);
    }
}
