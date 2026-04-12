package com.meuapi.games_api.controllers;

import com.meuapi.games_api.entities.Plataforma;
import com.meuapi.games_api.repositories.PlataformaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/plataformas")
@Tag(name = "Plataformas")
public class PlataformaController {

    @Autowired private PlataformaRepository repository;
    @Autowired private PagedResourcesAssembler<Plataforma> assembler;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Lista todas as plataformas", description = "Retorna uma lista paginada com links HATEOAS")
    @GetMapping
    public PagedModel<EntityModel<Plataforma>> listar(Pageable pageable) {
        return assembler.toModel(repository.findAll(pageable));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Busca plataforma por ID", description = "Retorna os detalhes de uma plataforma específica")
    @GetMapping("/{id}")
    public EntityModel<Plataforma> buscar(@PathVariable Long id) {
        Plataforma p = repository.findById(id).orElseThrow();
        return EntityModel.of(p, linkTo(methodOn(PlataformaController.class).buscar(id)).withSelfRel());
    }

    @Operation(summary = "Cadastra nova plataforma", description = "Cria uma nova plataforma para cadastrar um novo jogo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Plataforma>> criar(@RequestBody Plataforma p) {
        Plataforma nova = repository.save(p);
        EntityModel<Plataforma> model = EntityModel.of(nova,
                linkTo(methodOn(PlataformaController.class).buscar(nova.getId())).withSelfRel(),
                linkTo(methodOn(PlataformaController.class).listar(null)).withRel("lista"));
        return ResponseEntity.status(201).body(model);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Atualiza uma plataforma", description = "Permite alterar uma plataforma já existente")
    @PutMapping("/{id}")
    public EntityModel<Plataforma> atualizar(@PathVariable Long id, @RequestBody Plataforma nova) {
        return repository.findById(id).map(p -> {
            p.setNome(nova.getNome());
            return EntityModel.of(repository.save(p),
                    linkTo(methodOn(PlataformaController.class).buscar(id)).withSelfRel());
        }).orElseThrow(() -> new RuntimeException("Plataforma não encontrada"));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Consulta personalizada por nome", description = "Permite buscar uma plataforma pelo nome")
    @GetMapping("/busca")
    public ResponseEntity<?> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(repository.findByNomeContainingIgnoreCase(nome));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Plataforma excluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Exclui uma plataforma", description = "Remove permanentemente a plataforma do acervo")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
