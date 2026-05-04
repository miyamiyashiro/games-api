package com.meuapi.games_api.controllers;

import com.meuapi.games_api.dto.DetalhesJogoRequest;
import com.meuapi.games_api.entities.DetalhesJogo;
import com.meuapi.games_api.entities.Jogo;
import com.meuapi.games_api.exceptions.RecursoNaoEncontradoException;
import com.meuapi.games_api.repositories.DetalhesJogoRepository;
import com.meuapi.games_api.repositories.JogoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@Tag(name = "Detalhes dos Jogos")
@RequestMapping("/detalhes-jogos")
public class DetalhesJogoController {

    private final DetalhesJogoRepository repository;
    private final JogoRepository jogoRepository;
    private final PagedResourcesAssembler<DetalhesJogo> assembler;

    @Autowired
    public DetalhesJogoController(
            DetalhesJogoRepository repository,
            JogoRepository jogoRepository,
            PagedResourcesAssembler<DetalhesJogo> assembler
    ) {
        this.repository = repository;
        this.jogoRepository = jogoRepository;
        this.assembler = assembler;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhes listados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos")
    })
    @Operation(summary = "Lista os detalhes dos jogos", description = "Retorna uma lista paginada com links HATEOAS")
    @GetMapping
    public PagedModel<EntityModel<DetalhesJogo>> listarTodos(Pageable pageable) {
        Page<DetalhesJogo> detalhes = repository.findAll(pageable);
        return assembler.toModel(detalhes, this::criarModelo);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhes encontrados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Detalhes nao encontrados")
    })
    @Operation(summary = "Busca detalhes por ID", description = "Retorna os detalhes complementares de um jogo pelo ID")
    @GetMapping("/{id}")
    public EntityModel<DetalhesJogo> buscarPorId(@PathVariable Long id) {
        DetalhesJogo detalhes = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
        return criarModelo(detalhes);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhes encontrados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Detalhes nao encontrados para o jogo informado")
    })
    @Operation(summary = "Consulta personalizada por jogo", description = "Busca os detalhes complementares pelo ID do jogo")
    @GetMapping("/jogo/{jogoId}")
    public EntityModel<DetalhesJogo> buscarPorJogo(@PathVariable Long jogoId) {
        DetalhesJogo detalhes = repository.findByJogoId(jogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Detalhes nao encontrados para o jogo ID: " + jogoId));
        return criarModelo(detalhes);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Detalhes cadastrados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "409", description = "Conflito de idempotencia (Chave repetida com corpo diferente)"),
            @ApiResponse(responseCode = "200", description = "Operação já realizada anteriormente (Idempotência)")
    })
    @Operation(
            summary = "Cadastra detalhes de um jogo",
            description = "Cria o registro One-to-One de detalhes complementares usando o ID do jogo",
            parameters = {
                    @Parameter(
                            name = "Idempotency-Key",
                            description = "Chave única para evitar duplicidade (ex: UUID ou número)",
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string"),
                            required = false
                    )
            }
    )
    @PostMapping
    public ResponseEntity<EntityModel<DetalhesJogo>> criar(@Valid @RequestBody DetalhesJogoRequest request) {
        DetalhesJogo novo = new DetalhesJogo();
        preencherDetalhes(novo, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(criarModelo(repository.save(novo)));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhes atualizados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Detalhes nao encontrados")
    })
    @Operation(summary = "Atualiza detalhes de um jogo", description = "Altera descricao, idade minima, tempo medio e jogo vinculado")
    @PutMapping("/{id}")
    public EntityModel<DetalhesJogo> atualizar(@PathVariable Long id, @Valid @RequestBody DetalhesJogoRequest request) {
        return repository.findById(id).map(detalhes -> {
            preencherDetalhes(detalhes, request);
            return criarModelo(repository.save(detalhes));
        }).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Detalhes excluidos com sucesso"),
            @ApiResponse(responseCode = "404", description = "Detalhes nao encontrados")
    })
    @Operation(summary = "Exclui detalhes de um jogo", description = "Remove os detalhes complementares do acervo")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException(id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void preencherDetalhes(DetalhesJogo detalhes, DetalhesJogoRequest request) {
        Jogo jogo = jogoRepository.findById(request.jogoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo nao encontrado com ID: " + request.jogoId()));

        detalhes.setDescricao(request.descricao());
        detalhes.setIdadeMinima(request.idadeMinima());
        detalhes.setTempoMedioMinutos(request.tempoMedioMinutos());
        detalhes.setJogo(jogo);
    }

    private EntityModel<DetalhesJogo> criarModelo(DetalhesJogo detalhes) {
        return EntityModel.of(detalhes,
                linkTo(methodOn(DetalhesJogoController.class).buscarPorId(detalhes.getId())).withSelfRel(),
                linkTo(methodOn(DetalhesJogoController.class).listarTodos(null)).withRel("lista"),
                linkTo(methodOn(JogoController.class).buscarPorId(detalhes.getJogo().getId())).withRel("jogo"));
    }
}
