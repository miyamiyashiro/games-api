package com.meuapi.games_api.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filtro de Rate Limiting.
 * Limita cada IP a no máximo MAX_REQUESTS requisições por janela de tempo (WINDOW_MS).
 * Se o limite for excedido, o IP fica bloqueado por BLOCK_MS (30 segundos).
 * Retorna HTTP 429 enquanto bloqueado.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Número máximo de requisições permitidas na janela de tempo
    private static final int MAX_REQUESTS = 10;

    // Janela de tempo para contagem (60 segundos)
    private static final long WINDOW_MS = 60_000;

    // Tempo de bloqueio após exceder o limite (30 segundos)
    private static final long BLOCK_MS = 30_000;

    // Dados por IP: contador de requisições, início da janela e momento do bloqueio
    private static class IpData {
        AtomicInteger count = new AtomicInteger(0);
        long windowStart = System.currentTimeMillis();
        long blockedUntil = 0;
    }

    private final Map<String, IpData> ipDataMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = obterIpCliente(request);
        IpData data = ipDataMap.computeIfAbsent(ip, k -> new IpData());

        long agora = System.currentTimeMillis();

        synchronized (data) {
            // Verifica se o IP está bloqueado
            if (data.blockedUntil > 0 && agora < data.blockedUntil) {
                long segundosRestantes = (data.blockedUntil - agora) / 1000;
                escreverResposta429(response, ip, segundosRestantes);
                return;
            }

            // Reseta o bloqueio se já expirou
            if (data.blockedUntil > 0 && agora >= data.blockedUntil) {
                data.blockedUntil = 0;
                data.count.set(0);
                data.windowStart = agora;
            }

            // Reseta a janela de contagem se expirou
            if (agora - data.windowStart > WINDOW_MS) {
                data.count.set(0);
                data.windowStart = agora;
            }

            // Incrementa o contador
            int requisicoes = data.count.incrementAndGet();

            // Se excedeu o limite, bloqueia o IP por 30 segundos
            if (requisicoes > MAX_REQUESTS) {
                data.blockedUntil = agora + BLOCK_MS;
                escreverResposta429(response, ip, BLOCK_MS / 1000);
                return;
            }

            // Adiciona headers informativos
            response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(MAX_REQUESTS - requisicoes));
        }

        filterChain.doFilter(request, response);
    }

    private void escreverResposta429(HttpServletResponse response, String ip, long segundosRestantes)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(segundosRestantes));

        Map<String, Object> corpo = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 429,
                "erro", "Too Many Requests",
                "mensagem", "Voce excedeu o limite de requisicoes. Tente novamente em " + segundosRestantes + " segundos.",
                "detalhes", List.of("IP bloqueado: " + ip, "Retry-After: " + segundosRestantes + "s")
        );

        objectMapper.writeValue(response.getWriter(), corpo);
    }

    private String obterIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
