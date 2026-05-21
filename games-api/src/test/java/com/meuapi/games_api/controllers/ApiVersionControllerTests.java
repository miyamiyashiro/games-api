package com.meuapi.games_api.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiVersionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveExporStatusNaVersaoUm() throws Exception {
        mockMvc.perform(get("/api/v1/status")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.40");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value("v1"))
                .andExpect(jsonPath("$.mensagem").value("Games API em funcionamento"));
    }

    @Test
    void deveExporStatusNaVersaoDoisComContratoExpandido() throws Exception {
        mockMvc.perform(get("/api/v2/status")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.41");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value("v2"))
                .andExpect(jsonPath("$.servico").value("Games API"))
                .andExpect(jsonPath("$.recursos.autenticacao").value("X-API-Key"));
    }

    @Test
    void deveExporJogosNaVersaoUmComContratoSimplificado() throws Exception {
        mockMvc.perform(get("/api/v1/jogos")
                        .param("page", "0")
                        .param("size", "1")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.42");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].titulo").exists())
                .andExpect(jsonPath("$.content[0].categoria").exists())
                .andExpect(jsonPath("$.content[0].editora").doesNotExist())
                .andExpect(jsonPath("$.content[0].links").doesNotExist());
    }

    @Test
    void deveExporJogosNaVersaoDoisComContratoCompletoEHateoas() throws Exception {
        mockMvc.perform(get("/api/v2/jogos")
                        .param("page", "0")
                        .param("size", "1")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.43");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].titulo").exists())
                .andExpect(jsonPath("$.content[0].categoria").exists())
                .andExpect(jsonPath("$.content[0].editora").exists())
                .andExpect(jsonPath("$.content[0].plataformas").isArray())
                .andExpect(jsonPath("$.content[0].links").isArray());
    }
}
