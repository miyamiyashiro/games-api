package com.meuapi.games_api.filters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de idempotencia para operacoes de escrita.
 * A mesma Idempotency-Key so pode ser reutilizada para a mesma operacao e o mesmo JSON.
 */
@Component
@Order(3)
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
    private static final Set<String> METODOS_VERIFICADOS = Set.of("POST", "PUT", "PATCH");

    private record IdempotencyEntry(String method, String path, String normalizedBody) {
    }

    private final Map<String, IdempotencyEntry> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String metodo = request.getMethod().toUpperCase();

        if (!METODOS_VERIFICADOS.contains(metodo)) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader(IDEMPOTENCY_HEADER);

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        CachedBodyRequestWrapper requestWrapper = new CachedBodyRequestWrapper(request);
        IdempotencyEntry entradaAtual = new IdempotencyEntry(
                metodo,
                request.getRequestURI(),
                normalizarBody(requestWrapper.getBodyBytes())
        );
        IdempotencyEntry entradaAnterior = cache.get(idempotencyKey);

        if (entradaAnterior == null) {
            cache.put(idempotencyKey, entradaAtual);
            filterChain.doFilter(requestWrapper, response);
            return;
        }

        if (!entradaAnterior.method().equals(entradaAtual.method())
                || !entradaAnterior.path().equals(entradaAtual.path())) {
            escreverRespostaErro(response, HttpStatus.CONFLICT,
                    "Conflito de idempotência: esta chave já foi usada em outra operação.",
                    idempotencyKey);
            return;
        }

        if (!entradaAnterior.normalizedBody().equals(entradaAtual.normalizedBody())) {
            escreverRespostaErro(response, HttpStatus.CONFLICT,
                    "Conflito de idempotência: o corpo da requisição é diferente do original para esta chave.",
                    idempotencyKey);
            return;
        }

        escreverRespostaSucessoIdempotente(response, idempotencyKey);
    }

    private void escreverRespostaErro(HttpServletResponse response, HttpStatus status, String mensagem, String chave)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> corpo = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "erro", status.getReasonPhrase(),
                "mensagem", mensagem,
                "detalhes", List.of("Idempotency-Key: " + chave)
        );

        objectMapper.writeValue(response.getWriter(), corpo);
    }

    private void escreverRespostaSucessoIdempotente(HttpServletResponse response, String chave) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> corpo = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 200,
                "mensagem", "Operação já realizada anteriormente. O processamento foi ignorado para evitar duplicidade.",
                "idempotencyKey", chave
        );

        objectMapper.writeValue(response.getWriter(), corpo);
    }

    private String normalizarBody(byte[] body) throws IOException {
        if (body.length == 0) {
            return "";
        }

        JsonNode jsonNode = objectMapper.readTree(body);
        return objectMapper.writeValueAsString(ordenarJson(jsonNode));
    }

    private JsonNode ordenarJson(JsonNode node) {
        if (node.isObject()) {
            ObjectNode ordenado = objectMapper.createObjectNode();
            List<String> nomes = new ArrayList<>();
            node.fieldNames().forEachRemaining(nomes::add);
            Collections.sort(nomes);

            for (String nome : nomes) {
                ordenado.set(nome, ordenarJson(node.get(nome)));
            }
            return ordenado;
        }

        if (node.isArray()) {
            ArrayNode ordenado = objectMapper.createArrayNode();
            for (JsonNode item : node) {
                ordenado.add(ordenarJson(item));
            }
            return ordenado;
        }

        return node;
    }

    private static class CachedBodyRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        CachedBodyRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            this.cachedBody = request.getInputStream().readAllBytes();
        }

        byte[] getBodyBytes() {
            return cachedBody;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }

                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
