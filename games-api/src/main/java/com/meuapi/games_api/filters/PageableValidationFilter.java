package com.meuapi.games_api.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meuapi.games_api.exceptions.ApiErrorResponse;
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
import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
public class PageableValidationFilter extends OncePerRequestFilter {

    private static final int MAX_PAGE = 10_000;
    private static final int MAX_SIZE = 50;

    private final ObjectMapper objectMapper;

    public PageableValidationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!"GET".equalsIgnoreCase(request.getMethod()) || deveIgnorar(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        List<String> detalhes = validarPaginacao(request);
        if (!detalhes.isEmpty()) {
            escreverBadRequest(request, response, detalhes);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean deveIgnorar(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.equals("/")
                || path.equals("/index.html")
                || path.equals("/styles.css")
                || path.equals("/app.js")
                || path.equals("/favicon.ico")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/webjars")
                || path.startsWith("/h2-console");
    }

    private List<String> validarPaginacao(HttpServletRequest request) {
        List<String> detalhes = new ArrayList<>();

        if (request.getParameter("pageable") != null) {
            detalhes.add("Use os parametros page, size e sort separadamente. Nao envie um objeto JSON em pageable.");
        }

        validarInteiro(request, "page", 0, MAX_PAGE, detalhes);
        validarInteiro(request, "size", 1, MAX_SIZE, detalhes);

        return detalhes;
    }

    private void validarInteiro(
            HttpServletRequest request,
            String nome,
            int minimo,
            int maximo,
            List<String> detalhes
    ) {
        String valor = request.getParameter(nome);
        if (valor == null || valor.isBlank()) {
            return;
        }

        try {
            int numero = Integer.parseInt(valor);
            if (numero < minimo || numero > maximo) {
                detalhes.add(nome + " deve estar entre " + minimo + " e " + maximo + ".");
            }
        } catch (NumberFormatException ex) {
            detalhes.add(nome + " deve ser um numero inteiro.");
        }
    }

    private void escreverBadRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            List<String> detalhes
    ) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse corpo = new ApiErrorResponse(
                LocalDateTime.now().toString(),
                400,
                "Bad Request",
                "Parametros de paginacao invalidos",
                request.getRequestURI(),
                request.getMethod(),
                detalhes
        );

        objectMapper.writeValue(response.getWriter(), corpo);
    }
}
