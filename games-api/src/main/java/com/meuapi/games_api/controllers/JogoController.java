package com.meuapi.games_api.controllers;

import com.meuapi.games_api.dto.JogoRequest;
import com.meuapi.games_api.entities.Editora;
import com.meuapi.games_api.entities.Jogo;
import com.meuapi.games_api.entities.Plataforma;
import com.meuapi.games_api.exceptions.RecursoNaoEncontradoException;
import com.meuapi.games_api.repositories.EditoraRepository;
import com.meuapi.games_api.repositories.JogoRepository;
import com.meuapi.games_api.repositories.PlataformaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@Tag(name = "Jogos")
@RequestMapping("/jogos")
public class JogoController {

    private final JogoRepository repository;
    private final EditoraRepository editoraRepository;
    private final PlataformaRepository plataformaRepository;
    private final PagedResourcesAssembler<Jogo> pagedResourcesAssembler;

    @Autowired
    public JogoController(
            JogoRepository repository,
            EditoraRepository editoraRepository,
            PlataformaRepository plataformaRepository,
            PagedResourcesAssembler<Jogo> pagedResourcesAssembler
    ) {
        this.repository = repository;
        this.editoraRepository = editoraRepository;
        this.plataformaRepository = plataformaRepository;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogos listados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos")
    })
    @Operation(summary = "Lista todos os jogos", description = "Retorna uma lista paginada com links HATEOAS")
    @GetMapping
    public PagedModel<EntityModel<Jogo>> listarTodos(Pageable pageable) {
        Page<Jogo> jogos = repository.findAll(pageable);
        return pagedResourcesAssembler.toModel(jogos, this::criarModelo);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogo encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Jogo nao encontrado")
    })
    @Operation(summary = "Busca um jogo por ID", description = "Retorna os detalhes de um jogo especifico")
    @GetMapping("/{id}")
    public EntityModel<Jogo> buscarPorId(@PathVariable Long id) {
        Jogo jogo = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
        return criarModelo(jogo);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Jogo cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "409", description = "Conflito de idempotencia"), // Boa prática adicionar essa!
            @ApiResponse(responseCode = "200", description = "Operação já realizada (Idempotência)")
    })
    @Operation(
            summary = "Cadastra um novo jogo",
            description = "Cria um novo jogo no acervo usando IDs de editora e plataformas",
            parameters = {
                    @Parameter(
                            name = "Idempotency-Key",
                            description = "Chave única para evitar duplicidade (ex: 12345)",
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string"),
                            required = false
                    )
            }
    )
    @PostMapping
    public ResponseEntity<EntityModel<Jogo>> criar(@Valid @RequestBody JogoRequest request) {
        Jogo novo = new Jogo();
        preencherJogo(novo, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(criarModelo(repository.save(novo)));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Jogo nao encontrado")
    })
    @Operation(summary = "Atualiza um jogo", description = "Permite alterar titulo, categoria, editora e plataformas")
    @PutMapping("/{id}")
    public EntityModel<Jogo> atualizar(@PathVariable Long id, @Valid @RequestBody JogoRequest request) {
        return repository.findById(id)
                .map(jogo -> {
                    preencherJogo(jogo, request);
                    return criarModelo(repository.save(jogo));
                })
                .orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Jogo excluido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Jogo nao encontrado")
    })
    @Operation(summary = "Exclui um jogo", description = "Remove permanentemente o jogo do acervo")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException(id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parametro invalido")
    })
    @Operation(summary = "Consulta personalizada", description = "Busca jogos por parte do titulo, sem diferenciar maiusculas e minusculas")
    @GetMapping("/busca")
    public CollectionModel<EntityModel<Jogo>> buscarPorTitulo(@RequestParam String titulo) {
        List<EntityModel<Jogo>> jogos = repository.findByTituloContainingIgnoreCase(titulo).stream()
                .map(this::criarModelo)
                .toList();
        return CollectionModel.of(jogos,
                linkTo(methodOn(JogoController.class).buscarPorTitulo(titulo)).withSelfRel(),
                linkTo(methodOn(JogoController.class).listarTodos(null)).withRel("lista"));
    }

    private void preencherJogo(Jogo jogo, JogoRequest request) {
        Editora editora = editoraRepository.findById(request.editoraId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Editora nao encontrada com ID: " + request.editoraId()));
        List<Plataforma> plataformas = plataformaRepository.findAllById(request.plataformaIds());

        if (plataformas.size() != request.plataformaIds().size()) {
            throw new RecursoNaoEncontradoException("Uma ou mais plataformas nao foram encontradas");
        }

        jogo.setTitulo(request.titulo());
        jogo.setCategoria(request.categoria());
        jogo.setEditora(editora);
        jogo.setPlataformas(plataformas);
    }

    private EntityModel<Jogo> criarModelo(Jogo jogo) {
        return EntityModel.of(jogo,
                linkTo(methodOn(JogoController.class).buscarPorId(jogo.getId())).withSelfRel(),
                linkTo(methodOn(JogoController.class).listarTodos(null)).withRel("lista"));
    }
}
