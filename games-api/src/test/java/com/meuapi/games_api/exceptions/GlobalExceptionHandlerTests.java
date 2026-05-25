package com.meuapi.games_api.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
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
    void deveRetornarConflictParaEmailJaCadastrado() throws Exception {
        String email = "email-duplicado-%s@example.com".formatted(UUID.randomUUID());
        String body = """
                {
                  "nome": "Usuario Duplicado",
                  "email": "%s"
                }
                """.formatted(email);

        mockMvc.perform(post("/usuarios")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.89");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/usuarios")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.90");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value("Conflito com dados ja cadastrados"));
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
        String sufixo = UUID.randomUUID().toString();

        MvcResult usuarioCriado = mockMvc.perform(post("/usuarios")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.83");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario metodo nao permitido",
                                  "email": "metodo-nao-permitido-%s@example.com"
                                }
                                """.formatted(sufixo)))
                .andExpect(status().isCreated())
                .andReturn();

        String usuarioJson = usuarioCriado.getResponse().getContentAsString();
        String usuarioId = usuarioJson.replaceAll(".*\"id\":(\\d+).*", "$1");

        MvcResult chaveGerada = mockMvc.perform(post("/api-keys")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.83");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioId": %s
                                }
                                """.formatted(usuarioId)))
                .andExpect(status().isOk())
                .andReturn();

        String apiKeyJson = chaveGerada.getResponse().getContentAsString();
        String apiKey = apiKeyJson.replaceAll(".*\"apiKey\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(request(HttpMethod.POST, "/api-keys/{id}", usuarioId)
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.83");
                            return request;
                        })
                        .header("X-API-Key", apiKey))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.mensagem").value("Metodo HTTP nao permitido para este endpoint"))
                .andExpect(jsonPath("$.caminho").value("/api-keys/" + usuarioId))
                .andExpect(jsonPath("$.metodo").value("POST"));
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

    @Test
    void deveRetornarBadRequestParaEmailComFormatoInvalido() throws Exception {
        mockMvc.perform(get("/usuarios/email/123")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.86");
                            return request;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Nao foi possivel processar a requisicao"))
                .andExpect(jsonPath("$.detalhes[0]").value(containsString("email deve estar em formato valido")));
    }

    @Test
    void deveRetornarNotFoundParaEmailValidoNaoCadastrado() throws Exception {
        mockMvc.perform(get("/usuarios/email/nao-existe@example.com")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.87");
                            return request;
                        }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value(containsString("nao-existe@example.com")));
    }

    @Test
    void deveRetornarBadRequestQuandoBuscaRecebeApenasNumeros() throws Exception {
        mockMvc.perform(get("/jogos/busca")
                        .param("titulo", "12345")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.86");
                            return request;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Nao foi possivel processar a requisicao"))
                .andExpect(jsonPath("$.detalhes[0]").value(containsString("deve conter texto")));
    }

    @Test
    void deveRetornarNotFoundQuandoBuscaNaoTemResultado() throws Exception {
        mockMvc.perform(get("/editoras/busca")
                        .param("nome", "EditoraInexistenteParaTeste")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.87");
                            return request;
                        }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value(containsString("Nenhuma editora encontrada")))
                .andExpect(jsonPath("$.caminho").value("/editoras/busca"));
    }

    @Test
    void deveManterOkQuandoBuscaTemResultado() throws Exception {
        mockMvc.perform(get("/jogos/busca")
                        .param("titulo", "Catan")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.88");
                            return request;
                        }))
                .andExpect(status().isOk());
    }
}
