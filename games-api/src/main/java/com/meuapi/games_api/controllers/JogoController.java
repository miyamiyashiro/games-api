package com.meuapi.games_api.controllers;

import com.meuapi.games_api.entities.Jogo;
import com.meuapi.games_api.exceptions.RecursoNaoEncontradoException;
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

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
            @ApiResponse(responseCode = "200", description = "Jogos listados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    @Operation(summary = "Lista todos os jogos", description = "Retorna uma lista paginada com links HATEOAS")
    @GetMapping
    public PagedModel<EntityModel<Jogo>> listarTodos(Pageable pageable) {
        Page<Jogo> jogos = repository.findAll(pageable);
        return pagedResourcesAssembler.toModel(jogos, this::criarModelo);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogo encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    @Operation(summary = "Busca um jogo por ID", description = "Retorna os detalhes de um jogo específico")
    @GetMapping("/{id}")
    public EntityModel<Jogo> buscarPorId(@PathVariable Long id) {
        Jogo jogo = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
        return criarModelo(jogo);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Jogo cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @Operation(summary = "Cadastra um novo jogo", description = "Cria um novo jogo no acervo")
    @PostMapping
    public ResponseEntity<EntityModel<Jogo>> criar(@Valid @RequestBody Jogo jogo) {
        Jogo novo = repository.save(jogo);
        return ResponseEntity.status(HttpStatus.CREATED).body(criarModelo(novo));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jogo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    @Operation(summary = "Atualiza um jogo", description = "Permite alterar título, categoria, editora e plataformas")
    @PutMapping("/{id}")
    public EntityModel<Jogo> atualizar(@PathVariable Long id, @Valid @RequestBody Jogo novoJogo) {
        return repository.findById(id)
                .map(jogo -> {
                    jogo.setTitulo(novoJogo.getTitulo());
                    jogo.setCategoria(novoJogo.getCategoria());
                    jogo.setEditora(novoJogo.getEditora());
                    jogo.setPlataformas(novoJogo.getPlataformas());
                    return criarModelo(repository.save(jogo));
                })
                .orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Jogo excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado")
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
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido")
    })
    @Operation(summary = "Consulta personalizada", description = "Busca jogos por parte do título, sem diferenciar maiúsculas e minúsculas")
    @GetMapping("/busca")
    public CollectionModel<EntityModel<Jogo>> buscarPorTitulo(@RequestParam String titulo) {
        List<EntityModel<Jogo>> jogos = repository.findByTituloContainingIgnoreCase(titulo).stream()
                .map(this::criarModelo)
                .toList();
        return CollectionModel.of(jogos,
                linkTo(methodOn(JogoController.class).buscarPorTitulo(titulo)).withSelfRel(),
                linkTo(methodOn(JogoController.class).listarTodos(null)).withRel("lista"));
    }

    private EntityModel<Jogo> criarModelo(Jogo jogo) {
        return EntityModel.of(jogo,
                linkTo(methodOn(JogoController.class).buscarPorId(jogo.getId())).withSelfRel(),
                linkTo(methodOn(JogoController.class).listarTodos(null)).withRel("lista"));
    }
}
