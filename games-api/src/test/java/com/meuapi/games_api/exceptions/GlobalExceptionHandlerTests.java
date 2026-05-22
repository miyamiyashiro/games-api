package com.meuapi.games_api.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornarErroPadronizadoParaRecursoNaoEncontrado() throws Exception {
        mockMvc.perform(get("/jogos/999999")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.80");
                            return request;
                        }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Not Found"))
                .andExpect(jsonPath("$.mensagem").value(containsString("999999")))
                .andExpect(jsonPath("$.caminho").value("/jogos/999999"))
                .andExpect(jsonPath("$.metodo").value("GET"));
    }

    @Test
    void deveRetornarErroPadronizadoParaValidacao() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.81");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "",
                                  "email": "email-invalido"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Dados invalidos na requisicao"))
                .andExpect(jsonPath("$.detalhes").isArray())
                .andExpect(jsonPath("$.caminho").value("/usuarios"))
                .andExpect(jsonPath("$.metodo").value("POST"));
    }

    @Test
    void deveRetornarErroPadronizadoParaJsonInvalidoComIdempotencyKey() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.82");
                            return request;
                        })
                        .header("Idempotency-Key", "json-invalido-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Json quebrado\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("JSON invalido ou mal formatado"))
                .andExpect(jsonPath("$.caminho").value("/usuarios"));
    }

    @Test
    void deveRetornarErroPadronizadoParaMetodoNaoPermitido() throws Exception {
        mockMvc.perform(get("/usuarios/1/api-key")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.83");
                            return request;
                        }))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.mensagem").value("Metodo HTTP nao permitido para este endpoint"))
                .andExpect(jsonPath("$.caminho").value("/usuarios/1/api-key"))
                .andExpect(jsonPath("$.metodo").value("GET"));
    }

    @Test
    void deveRetornarBadRequestParaPaginacaoAbsurda() throws Exception {
        mockMvc.perform(get("/plataformas")
                        .param("page", "1073741824")
                        .param("size", "1073741824")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.84");
                            return request;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Parametros de paginacao invalidos"))
                .andExpect(jsonPath("$.caminho").value("/plataformas"))
                .andExpect(jsonPath("$.metodo").value("GET"));
    }

    @Test
    void deveRetornarBadRequestParaPageableComoJson() throws Exception {
        mockMvc.perform(get("/plataformas")
                        .param("pageable", "{\"page\":1073741824,\"size\":1073741824,\"sort\":[\"string\"]}")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.85");
                            return request;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Parametros de paginacao invalidos"))
                .andExpect(jsonPath("$.detalhes[0]").value(containsString("page, size e sort")));
    }
}
