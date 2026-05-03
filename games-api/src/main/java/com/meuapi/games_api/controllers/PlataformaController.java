package com.meuapi.games_api.controllers;

import com.meuapi.games_api.dto.PlataformaRequest;
import com.meuapi.games_api.entities.Plataforma;
import com.meuapi.games_api.exceptions.RecursoNaoEncontradoException;
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

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/plataformas")
@Tag(name = "Plataformas")
public class PlataformaController {

    private final PlataformaRepository repository;
    private final PagedResourcesAssembler<Plataforma> assembler;

    @Autowired
    public PlataformaController(PlataformaRepository repository, PagedResourcesAssembler<Plataforma> assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plataformas listadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos")
    })
    @Operation(summary = "Lista todas as plataformas", description = "Retorna uma lista paginada com links HATEOAS")
    @GetMapping
    public PagedModel<EntityModel<Plataforma>> listar(Pageable pageable) {
        Page<Plataforma> plataformas = repository.findAll(pageable);
        return assembler.toModel(plataformas, this::criarModelo);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plataforma encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Plataforma nao encontrada")
    })
    @Operation(summary = "Busca plataforma por ID", description = "Retorna os detalhes de uma plataforma especifica")
    @GetMapping("/{id}")
    public EntityModel<Plataforma> buscar(@PathVariable Long id) {
        Plataforma plataforma = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
        return criarModelo(plataforma);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Plataforma cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @Operation(summary = "Cadastra nova plataforma", description = "Cria uma nova plataforma para vinculo com jogos")
    @PostMapping
    public ResponseEntity<EntityModel<Plataforma>> criar(@Valid @RequestBody PlataformaRequest request) {
        Plataforma plataforma = new Plataforma();
        plataforma.setNome(request.nome());
        return ResponseEntity.status(HttpStatus.CREATED).body(criarModelo(repository.save(plataforma)));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plataforma atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Plataforma nao encontrada")
    })
    @Operation(summary = "Atualiza uma plataforma", description = "Permite alterar uma plataforma existente")
    @PutMapping("/{id}")
    public EntityModel<Plataforma> atualizar(@PathVariable Long id, @Valid @RequestBody PlataformaRequest request) {
        return repository.findById(id).map(plataforma -> {
            plataforma.setNome(request.nome());
            return criarModelo(repository.save(plataforma));
        }).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parametro invalido")
    })
    @Operation(summary = "Consulta personalizada por nome", description = "Busca plataformas por parte do nome")
    @GetMapping("/busca")
    public CollectionModel<EntityModel<Plataforma>> buscarPorNome(@RequestParam String nome) {
        List<EntityModel<Plataforma>> plataformas = repository.findByNomeContainingIgnoreCase(nome).stream()
                .map(this::criarModelo)
                .toList();
        return CollectionModel.of(plataformas,
                linkTo(methodOn(PlataformaController.class).buscarPorNome(nome)).withSelfRel(),
                linkTo(methodOn(PlataformaController.class).listar(null)).withRel("lista"));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Plataforma excluida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Plataforma nao encontrada")
    })
    @Operation(summary = "Exclui uma plataforma", description = "Remove permanentemente a plataforma do acervo")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException(id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<Plataforma> criarModelo(Plataforma plataforma) {
        return EntityModel.of(plataforma,
                linkTo(methodOn(PlataformaController.class).buscar(plataforma.getId())).withSelfRel(),
                linkTo(methodOn(PlataformaController.class).listar(null)).withRel("lista"));
    }
}
