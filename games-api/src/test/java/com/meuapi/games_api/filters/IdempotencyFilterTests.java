package com.meuapi.games_api.filters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IdempotencyFilterTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveIgnorarRequisicaoRepetidaComMesmoJsonSemantico() throws Exception {
        String key = UUID.randomUUID().toString();
        String ip = "203.0.113.20";

        mockMvc.perform(post("/usuarios")
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        })
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario Idempotente",
                                  "email": "idempotente-1@example.com"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/usuarios")
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        })
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"idempotente-1@example.com","nome":"Usuario Idempotente"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.idempotencyKey").value(key));
    }

    @Test
    void deveRetornarConflitoQuandoMesmaChaveForUsadaComJsonDiferente() throws Exception {
        String key = UUID.randomUUID().toString();
        String ip = "203.0.113.21";

        mockMvc.perform(post("/usuarios")
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        })
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario Original",
                                  "email": "idempotente-2@example.com"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/usuarios")
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        })
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario Alterado",
                                  "email": "idempotente-2@example.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Conflict"));
    }
}
