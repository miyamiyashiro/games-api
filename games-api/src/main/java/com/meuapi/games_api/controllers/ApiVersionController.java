package com.meuapi.games_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Tag(name = "Versionamento")
public class ApiVersionController {

    @Operation(summary = "Status da API v1", description = "Demonstra versionamento por URL na versao 1")
    @GetMapping("/api/v1/status")
    public ResponseEntity<Map<String, Object>> statusV1() {
        return ResponseEntity.ok(Map.of(
                "versao", "v1",
                "status", "online",
                "mensagem", "Games API em funcionamento"
        ));
    }

    @Operation(summary = "Status da API v2", description = "Demonstra versionamento por URL na versao 2")
    @GetMapping("/api/v2/status")
    public ResponseEntity<Map<String, Object>> statusV2() {
        return ResponseEntity.ok(Map.of(
                "versao", "v2",
                "status", "online",
                "servico", "Games API",
                "recursos", Map.of(
                        "autenticacao", "X-API-Key",
                        "rateLimit", "10 requisicoes por minuto",
                        "idempotencia", "Idempotency-Key"
                ),
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
