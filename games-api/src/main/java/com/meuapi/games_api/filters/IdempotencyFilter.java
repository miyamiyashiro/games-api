package com.meuapi.games_api.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de Idempotência Reescrito.
 *
 * Lógica aplicada:
 * 1. Chave nova -> Salva body no cache e segue para o Controller (201 Created).
 * 2. Chave repetida + Body diferente -> 409 Conflict (Erro de integridade).
 * 3. Chave repetida + Body igual -> 200 OK (Ignora processamento repetido).
 */
@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
    private static final Set<String> METODOS_VERIFICADOS = Set.of("POST", "PUT", "PATCH");

    private final Map<String, byte[]> cache = new ConcurrentHashMap<>();
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

        // Wrapper para permitir múltiplas leituras do Body
        CachedBodyRequestWrapper requestWrapper = new CachedBodyRequestWrapper(request);
        byte[] bodyAtual = requestWrapper.getBodyBytes();
        byte[] bodyAnterior = cache.get(idempotencyKey);

        if (bodyAnterior == null) {
            // Caso 1: Chave inédita. Armazenamos e deixamos o Controller trabalhar.
            cache.put(idempotencyKey, bodyAtual);
            filterChain.doFilter(requestWrapper, response);
        } else if (!Arrays.equals(bodyAnterior, bodyAtual)) {
            // Caso 2: Tentativa de mudar os dados usando a mesma chave.
            escreverRespostaErro(response, HttpStatus.CONFLICT,
                    "Conflito de idempotencia: o corpo da requisicao e diferente do original para esta chave.",
                    idempotencyKey);
        } else {
            // Caso 3: Requisição idêntica já processada.
            // Retornamos 200 direto do filtro para não duplicar no banco.
            escreverRespostaSucessoIdempotente(response, idempotencyKey);
        }
    }

    private void escreverRespostaErro(HttpServletResponse response, HttpStatus status, String mensagem, String chave) throws IOException {
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
                "mensagem", "Operacao ja realizada anteriormente. O processamento foi ignorado para evitar duplicidade.",
                "idempotencyKey", chave
        );

        objectMapper.writeValue(response.getWriter(), corpo);
    }

    /**
     * Wrapper interno para cachear o stream do corpo da requisição.
     */
    private static class CachedBodyRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        public CachedBodyRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            this.cachedBody = request.getInputStream().readAllBytes();
        }

        public byte[] getBodyBytes() {
            return cachedBody;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override public int read() { return byteArrayInputStream.read(); }
                @Override public boolean isFinished() { return byteArrayInputStream.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener listener) {}
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }
    }
}
