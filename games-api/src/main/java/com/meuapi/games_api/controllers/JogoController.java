package com.meuapi.games_api.controllers;

import com.meuapi.games_api.entities.Jogo;
import com.meuapi.games_api.repositories.JogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import java.util.List;
import java.util.stream.Collectors;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@Tag(name = "Jogos")
@RequestMapping("/jogos")
public class JogoController {

    private final JogoRepository repository;
    private final PagedResourcesAssembler<Jogo> pagedResourcesAssembler;

    @Autowired
    public JogoController(JogoRepository repository, PagedResourcesAssembler<Jogo> pagedResourcesAssembler) {
        this.repository = repository;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Lista todos os jogos", description = "Retorna uma lista paginada com links HATEOAS")
    @GetMapping
    public PagedModel<EntityModel<Jogo>> listarTodos(Pageable pageable) {
        Page<Jogo> jogos = repository.findAll(pageable);
        return pagedResourcesAssembler.toModel(jogos,
                jogo -> EntityModel.of(jogo,
                        linkTo(methodOn(JogoController.class).buscarPorId(jogo.getId())).withSelfRel()));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Busca um jogo por ID", description = "Retorna os detalhes de um jogo específico")
    @GetMapping("/{id}")
    public EntityModel<Jogo> buscarPorId(@PathVariable Long id) {
        Jogo jogo = repository.findById(id).orElseThrow();
        return EntityModel.of(jogo,
                linkTo(methodOn(JogoController.class).buscarPorId(id)).withSelfRel());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Atualiza um jogo", description = "Permite alterar título ou categoria de um jogo existente")
    @PutMapping("/{id}")
    public EntityModel<Jogo> atualizar(@PathVariable Long id, @RequestBody Jogo novoJogo) {
        return repository.findById(id)
                .map(jogo -> {
                    jogo.setTitulo(novoJogo.getTitulo());
                    jogo.setCategoria(novoJogo.getCategoria());
                    Jogo atualizado = repository.save(jogo);
                    return EntityModel.of(atualizado,
                            linkTo(methodOn(JogoController.class).buscarPorId(id)).withSelfRel());
                })
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Jogo excluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Exclui um jogo", description = "Remove permanentemente o jogo do acervo")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Consulta personalizada", description = "Busca jogos por parte do título (case-insensitive)")
    @GetMapping("/busca")
    public CollectionModel<EntityModel<Jogo>> buscarPorTitulo(@RequestParam String titulo) {
        List<EntityModel<Jogo>> jogos = repository.findByTituloContainingIgnoreCase(titulo).stream()
                .map(jogo -> EntityModel.of(jogo,
                        linkTo(methodOn(JogoController.class).buscarPorId(jogo.getId())).withSelfRel()))
                .collect(Collectors.toList());
        return CollectionModel.of(jogos);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Cadastra um novo jogo", description = "Cria um novo jogo no acervo para o usuário escolher")
    @PostMapping
    public ResponseEntity<EntityModel<Jogo>> criar(@RequestBody Jogo jogo) {
        Jogo novo = repository.save(jogo);
        EntityModel<Jogo> model = EntityModel.of(novo,
                linkTo(methodOn(JogoController.class).buscarPorId(novo.getId())).withSelfRel(),
                linkTo(methodOn(JogoController.class).listarTodos(null)).withRel("lista"));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }
}