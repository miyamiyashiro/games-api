package com.meuapi.games_api.controllers;

import com.meuapi.games_api.entities.Editora;
import com.meuapi.games_api.repositories.EditoraRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@Tag(name = "Editora")
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
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Lista todos as editoras", description = "Retorna uma lista paginada com links HATEOAS")
    @GetMapping
    public PagedModel<EntityModel<Editora>> listarTodas(Pageable pageable) {
        Page<Editora> editoras = repository.findAll(pageable);
        return assembler.toModel(editoras,
                e -> EntityModel.of(e, linkTo(methodOn(EditoraController.class).buscarPorId(e.getId())).withSelfRel()));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Busca uma editora por ID", description = "Retorna os detalhes de uma editora específica")
    @GetMapping("/{id}")
    public EntityModel<Editora> buscarPorId(@PathVariable Long id) {
        Editora editora = repository.findById(id).orElseThrow();
        return EntityModel.of(editora, linkTo(methodOn(EditoraController.class).buscarPorId(id)).withSelfRel());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Cadastra uma nova editora", description = "Cria uma nova editora no acervo para cadastrar um novo jogo")
    @PostMapping
    public ResponseEntity<EntityModel<Editora>> criar(@RequestBody Editora editora) {
        Editora nova = repository.save(editora);
        EntityModel<Editora> model = EntityModel.of(nova,
                linkTo(methodOn(EditoraController.class).buscarPorId(nova.getId())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Editora excluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Exclui um editora", description = "Remove permanentemente a editora do acervo")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Busca editoras por parte do nome", description = "Permite buscar uma editora pelo nome")
    @GetMapping("/busca")
    public ResponseEntity<?> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(repository.findByNomeContainingIgnoreCase(nome));
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Atualiza uma editora", description = "Permite alterar uma editora já existente")
    @PutMapping("/{id}")
    public EntityModel<Editora> atualizar(@PathVariable Long id, @RequestBody Editora nova) {
        return repository.findById(id).map(e -> {
            e.setNome(nova.getNome());
            return EntityModel.of(repository.save(e),
                    linkTo(methodOn(EditoraController.class).buscarPorId(id)).withSelfRel());
        }).orElseThrow(() -> new RuntimeException("Editora não encontrada"));
    }
}
