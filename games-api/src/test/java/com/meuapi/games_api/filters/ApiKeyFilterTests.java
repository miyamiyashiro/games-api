package com.meuapi.games_api.filters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiKeyFilterTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveBloquearOperacaoProtegidaSemApiKey() throws Exception {
        mockMvc.perform(post("/editoras")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.30");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Editora sem chave"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "ApiKey realm=\"Games API\""))
                .andExpect(content().string(containsString("X-API-Key")));
    }

    @Test
    void devePermitirOperacaoProtegidaComApiKeyValida() throws Exception {
        String sufixo = UUID.randomUUID().toString();

        MvcResult usuarioCriado = mockMvc.perform(post("/usuarios")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.31");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario com API Key",
                                  "email": "api-key-%s@example.com"
                                }
                                """.formatted(sufixo)))
                .andExpect(status().isCreated())
                .andReturn();

        String usuarioJson = usuarioCriado.getResponse().getContentAsString();
        String usuarioId = usuarioJson.replaceAll(".*\"id\":(\\d+).*", "$1");

        MvcResult chaveGerada = mockMvc.perform(post("/usuarios/{id}/api-key", usuarioId)
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.31");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiKey").isString())
                .andReturn();

        String apiKeyJson = chaveGerada.getResponse().getContentAsString();
        String apiKey = apiKeyJson.replaceAll(".*\"apiKey\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/editoras")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.31");
                            return request;
                        })
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Editora protegida %s"
                                }
                                """.formatted(sufixo)))
                .andExpect(status().isCreated());
    }
}
