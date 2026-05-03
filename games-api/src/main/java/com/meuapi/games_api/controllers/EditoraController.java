package com.meuapi.games_api.controllers;

import com.meuapi.games_api.dto.EditoraRequest;
import com.meuapi.games_api.entities.Editora;
import com.meuapi.games_api.exceptions.RecursoNaoEncontradoException;
import com.meuapi.games_api.repositories.EditoraRepository;
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
@Tag(name = "Editoras")
@RequestMapping("/editoras")
public class EditoraController {

    private final EditoraRepository repository;
    private final PagedResourcesAssembler<Editora> assembler;

    @Autowired
    public EditoraController(EditoraRepository repository, PagedResourcesAssembler<Editora> assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Editoras listadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos")
    })
    @Operation(summary = "Lista todas as editoras", description = "Retorna uma lista paginada com links HATEOAS")
    @GetMapping
    public PagedModel<EntityModel<Editora>> listarTodas(Pageable pageable) {
        Page<Editora> editoras = repository.findAll(pageable);
        return assembler.toModel(editoras, this::criarModelo);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Editora encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Editora nao encontrada")
    })
    @Operation(summary = "Busca uma editora por ID", description = "Retorna os detalhes de uma editora especifica")
    @GetMapping("/{id}")
    public EntityModel<Editora> buscarPorId(@PathVariable Long id) {
        Editora editora = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
        return criarModelo(editora);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Editora cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @Operation(summary = "Cadastra uma nova editora", description = "Cria uma editora para vinculo com jogos")
    @PostMapping
    public ResponseEntity<EntityModel<Editora>> criar(@Valid @RequestBody EditoraRequest request) {
        Editora editora = new Editora();
        editora.setNome(request.nome());
        return ResponseEntity.status(HttpStatus.CREATED).body(criarModelo(repository.save(editora)));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Editora atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Editora nao encontrada")
    })
    @Operation(summary = "Atualiza uma editora", description = "Permite alterar o nome de uma editora existente")
    @PutMapping("/{id}")
    public EntityModel<Editora> atualizar(@PathVariable Long id, @Valid @RequestBody EditoraRequest request) {
        return repository.findById(id).map(editora -> {
            editora.setNome(request.nome());
            return criarModelo(repository.save(editora));
        }).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Editora excluida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Editora nao encontrada")
    })
    @Operation(summary = "Exclui uma editora", description = "Remove permanentemente a editora do acervo")
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
    @Operation(summary = "Consulta personalizada por nome", description = "Busca editoras por parte do nome")
    @GetMapping("/busca")
    public CollectionModel<EntityModel<Editora>> buscarPorNome(@RequestParam String nome) {
        List<EntityModel<Editora>> editoras = repository.findByNomeContainingIgnoreCase(nome).stream()
                .map(this::criarModelo)
                .toList();
        return CollectionModel.of(editoras,
                linkTo(methodOn(EditoraController.class).buscarPorNome(nome)).withSelfRel(),
                linkTo(methodOn(EditoraController.class).listarTodas(null)).withRel("lista"));
    }

    private EntityModel<Editora> criarModelo(Editora editora) {
        return EntityModel.of(editora,
                linkTo(methodOn(EditoraController.class).buscarPorId(editora.getId())).withSelfRel(),
                linkTo(methodOn(EditoraController.class).listarTodas(null)).withRel("lista"));
    }
}
