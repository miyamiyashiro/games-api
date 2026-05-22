package com.meuapi.games_api.controllers;

import com.meuapi.games_api.dto.JogoV1Response;
import com.meuapi.games_api.dto.JogoV2Response;
import com.meuapi.games_api.entities.DetalhesJogo;
import com.meuapi.games_api.entities.Jogo;
import com.meuapi.games_api.entities.Plataforma;
import com.meuapi.games_api.exceptions.RecursoNaoEncontradoException;
import com.meuapi.games_api.repositories.JogoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@Tag(
        name = "Jogos Versionados",
        description = "Demonstra versionamento real do recurso Jogos: v1 simplificada e v2 completa com HATEOAS."
)
public class JogoVersionadoController {

    private final JogoRepository repository;

    public JogoVersionadoController(JogoRepository repository) {
        this.repository = repository;
    }

    @Operation(
            summary = "[v1] Lista jogos em contrato simplificado",
            description = "Versao 1 do recurso Jogos. Retorna apenas id, titulo e categoria."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogos listados na versao 1"),
            @ApiResponse(responseCode = "429", description = "Muitas requisicoes", content = @Content)
    })
    @GetMapping("/api/v1/jogos")
    public ResponseEntity<Page<JogoV1Response>> listarV1(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .body(repository.findAll(pageable).map(this::toV1));
    }

    @Operation(
            summary = "[v1] Busca jogo por ID em contrato simplificado",
            description = "Versao 1 do recurso Jogos. Retorna apenas id, titulo e categoria."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogo encontrado na versao 1"),
            @ApiResponse(responseCode = "404", description = "Jogo nao encontrado", content = @Content),
            @ApiResponse(responseCode = "429", description = "Muitas requisicoes", content = @Content)
    })
    @GetMapping("/api/v1/jogos/{id}")
    public ResponseEntity<JogoV1Response> buscarV1(
            @Parameter(description = "ID do jogo", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok()
                .header("X-API-Version", "v1")
                .body(toV1(buscarJogo(id)));
    }

    @Operation(
            summary = "[v2] Lista jogos em contrato completo",
            description = "Versao 2 do recurso Jogos. Retorna editora, plataformas, detalhes complementares e links HATEOAS."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogos listados na versao 2"),
            @ApiResponse(responseCode = "429", description = "Muitas requisicoes", content = @Content)
    })
    @GetMapping("/api/v2/jogos")
    public ResponseEntity<Page<JogoV2Response>> listarV2(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok()
                .header("X-API-Version", "v2")
                .body(repository.findAll(pageable).map(this::toV2));
    }

    @Operation(
            summary = "[v2] Busca jogo por ID em contrato completo",
            description = "Versao 2 do recurso Jogos. Retorna todos os campos da resposta v2 e links HATEOAS."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogo encontrado na versao 2"),
            @ApiResponse(responseCode = "404", description = "Jogo nao encontrado", content = @Content),
            @ApiResponse(responseCode = "429", description = "Muitas requisicoes", content = @Content)
    })
    @GetMapping("/api/v2/jogos/{id}")
    public ResponseEntity<JogoV2Response> buscarV2(
            @Parameter(description = "ID do jogo", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok()
                .header("X-API-Version", "v2")
                .body(toV2(buscarJogo(id)));
    }

    private Jogo buscarJogo(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    private JogoV1Response toV1(Jogo jogo) {
        return new JogoV1Response(jogo.getId(), jogo.getTitulo(), jogo.getCategoria());
    }

    private JogoV2Response toV2(Jogo jogo) {
        DetalhesJogo detalhes = jogo.getDetalhes();

        JogoV2Response response = new JogoV2Response(
                jogo.getId(),
                jogo.getTitulo(),
                jogo.getCategoria(),
                jogo.getEditora() != null ? jogo.getEditora().getNome() : null,
                jogo.getPlataformas().stream().map(Plataforma::getNome).toList(),
                detalhes != null ? detalhes.getDescricao() : null,
                detalhes != null ? detalhes.getIdadeMinima() : null,
                detalhes != null ? detalhes.getTempoMedioMinutos() : null
        );

        response.add(linkTo(methodOn(JogoVersionadoController.class).buscarV2(jogo.getId())).withSelfRel());
        response.add(linkTo(methodOn(JogoVersionadoController.class).listarV2(Pageable.unpaged())).withRel("lista-v2"));
        response.add(linkTo(methodOn(JogoVersionadoController.class).buscarV1(jogo.getId())).withRel("versao-simplificada"));
        response.add(linkTo(methodOn(JogoController.class).buscarPorId(jogo.getId())).withRel("endpoint-crud"));

        return response;
    }
}
