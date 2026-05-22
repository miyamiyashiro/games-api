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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000;
    private static final long BLOCK_MS = 30_000;
    private static final String HEADER_LIMIT = "X-RateLimit-Limit";
    private static final String HEADER_REMAINING = "X-RateLimit-Remaining";
    private static final String HEADER_RESET = "X-RateLimit-Reset";
    private static final String HEADER_RETRY_AFTER = "Retry-After";

    private static class IpData {
        AtomicInteger count = new AtomicInteger(0);
        long windowStart = System.currentTimeMillis();
        long blockedUntil = 0;
    }

    private final Map<String, IpData> ipDataMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = obterIpCliente(request);
        IpData data = ipDataMap.computeIfAbsent(ip, key -> new IpData());
        long now = System.currentTimeMillis();

        synchronized (data) {
            if (data.blockedUntil > 0 && now < data.blockedUntil) {
                long secondsRemaining = secondsUntil(data.blockedUntil, now);
                setRateLimitHeaders(response, 0, data.blockedUntil);
                escreverResposta429(request, response, ip, secondsRemaining);
                return;
            }

            if (data.blockedUntil > 0 && now >= data.blockedUntil) {
                data.blockedUntil = 0;
                data.count.set(0);
                data.windowStart = now;
            }

            if (now - data.windowStart >= WINDOW_MS) {
                data.count.set(0);
                data.windowStart = now;
            }

            int requests = data.count.incrementAndGet();

            if (requests > MAX_REQUESTS) {
                data.blockedUntil = now + BLOCK_MS;
                setRateLimitHeaders(response, 0, data.blockedUntil);
                escreverResposta429(request, response, ip, secondsUntil(data.blockedUntil, now));
                return;
            }

            long resetAt = data.windowStart + WINDOW_MS;
            setRateLimitHeaders(response, MAX_REQUESTS - requests, resetAt);
        }

        filterChain.doFilter(request, response);
    }

    private void setRateLimitHeaders(HttpServletResponse response, int remaining, long resetAtMillis) {
        response.setHeader(HEADER_LIMIT, String.valueOf(MAX_REQUESTS));
        response.setHeader(HEADER_REMAINING, String.valueOf(Math.max(remaining, 0)));
        response.setHeader(HEADER_RESET, String.valueOf(resetAtMillis / 1000));
    }

    private void escreverResposta429(
            HttpServletRequest request,
            HttpServletResponse response,
            String ip,
            long segundosRestantes
    ) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HEADER_RETRY_AFTER, String.valueOf(segundosRestantes));

        ApiErrorResponse corpo = new ApiErrorResponse(
                LocalDateTime.now().toString(),
                429,
                "Too Many Requests",
                "Voce excedeu o limite de requisicoes. Tente novamente em " + segundosRestantes + " segundos.",
                request.getRequestURI(),
                request.getMethod(),
                List.of("IP bloqueado: " + ip, "Retry-After: " + segundosRestantes + "s")
        );

        objectMapper.writeValue(response.getWriter(), corpo);
    }

    private long secondsUntil(long targetMillis, long nowMillis) {
        return Math.max(1, (long) Math.ceil((targetMillis - nowMillis) / 1000.0));
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
