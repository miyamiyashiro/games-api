package com.meuapi.games_api.controllers;

import com.meuapi.games_api.entities.Usuario;
import com.meuapi.games_api.repositories.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@Tag(name = "Usuários")
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository repository;
    private final PagedResourcesAssembler<Usuario> pagedResourcesAssembler;

    @Autowired
    public UsuarioController(UsuarioRepository repository, PagedResourcesAssembler<Usuario> pagedResourcesAssembler) {
        this.repository = repository;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Lista todos os usuários", description = "Retorna uma lista paginada com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Usuario>>> listarTodos(Pageable pageable) {
        Page<Usuario> usuarios = repository.findAll(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(usuarios));
    }

    @Operation(summary = "Cadastra um novo usuário", description = "Cria um perfil de cliente para realizar empréstimos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já cadastrado")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Usuario>> criar(@Valid @RequestBody Usuario usuario) {
        Usuario novo = repository.save(usuario);
        EntityModel<Usuario> model = EntityModel.of(novo,
                linkTo(methodOn(UsuarioController.class).buscarPorId(novo.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listarTodos(null)).withRel("lista"));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Busca usuário por Id", description = "Retorna os detalhes de um usuário específico")
    @GetMapping("/{id}")
    public EntityModel<Usuario> buscarPorId(@PathVariable Long id) {
        Usuario usuario = repository.findById(id).orElseThrow();
        return EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).buscarPorId(id)).withSelfRel());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @Operation(summary = "Deletar usuário", description = "Remove permanentemente o usuário do acervo")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @Operation(summary = "Busca um usuário pelo email", description = "Consulta personalizada onde você pode buscar o usuário pelo email")
    @GetMapping("/email/{email}")
    public ResponseEntity<EntityModel<Usuario>> buscarPorEmail(@PathVariable String email) {
        Usuario u = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return ResponseEntity.ok(EntityModel.of(u,
                linkTo(methodOn(UsuarioController.class).buscarPorEmail(email)).withSelfRel()));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Atualiza dados do usuário", description = "Altera nome ou e-mail de um usuário cadastrado")
    @PutMapping("/{id}")
    public EntityModel<Usuario> atualizar(@PathVariable Long id, @RequestBody Usuario novo) {
        return repository.findById(id).map(u -> {
            u.setNome(novo.getNome());
            u.setEmail(novo.getEmail());
            return EntityModel.of(repository.save(u),
                    linkTo(methodOn(UsuarioController.class).buscarPorId(id)).withSelfRel());
        }).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}