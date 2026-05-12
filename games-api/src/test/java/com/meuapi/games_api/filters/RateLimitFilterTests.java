package com.meuapi.games_api.filters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitFilterTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveBloquearIpPorTrintaSegundosQuandoExcederLimite() throws Exception {
        String ip = "203.0.113.10";

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/jogos").with(request -> {
                        request.setRemoteAddr(ip);
                        return request;
                    }))
                    .andExpect(status().isOk())
                    .andExpect(header().string("X-RateLimit-Limit", "10"));
        }

        mockMvc.perform(get("/jogos").with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                }))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("X-RateLimit-Limit", "10"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(header().string("Retry-After", containsString("30")));
    }
}
