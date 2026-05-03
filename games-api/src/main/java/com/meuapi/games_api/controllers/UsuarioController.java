package com.meuapi.games_api.controllers;

import com.meuapi.games_api.dto.UsuarioRequest;
import com.meuapi.games_api.entities.Usuario;
import com.meuapi.games_api.exceptions.RecursoNaoEncontradoException;
import com.meuapi.games_api.repositories.UsuarioRepository;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@Tag(name = "Usuarios")
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
            @ApiResponse(responseCode = "200", description = "Usuarios listados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos")
    })
    @Operation(summary = "Lista todos os usuarios", description = "Retorna uma lista paginada com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Usuario>>> listarTodos(Pageable pageable) {
        Page<Usuario> usuarios = repository.findAll(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(usuarios, this::criarModelo));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou e-mail ja cadastrado")
    })
    @Operation(summary = "Cadastra um novo usuario", description = "Cria um perfil de cliente para realizar emprestimos")
    @PostMapping
    public ResponseEntity<EntityModel<Usuario>> criar(@Valid @RequestBody UsuarioRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(criarModelo(repository.save(usuario)));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado")
    })
    @Operation(summary = "Busca usuario por ID", description = "Retorna os detalhes de um usuario especifico")
    @GetMapping("/{id}")
    public EntityModel<Usuario> buscarPorId(@PathVariable Long id) {
        Usuario usuario = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
        return criarModelo(usuario);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario excluido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado")
    })
    @Operation(summary = "Deleta usuario", description = "Remove permanentemente o usuario do acervo")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException(id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado")
    })
    @Operation(summary = "Busca usuario por e-mail", description = "Consulta personalizada por e-mail exato")
    @GetMapping("/email/{email}")
    public ResponseEntity<EntityModel<Usuario>> buscarPorEmail(@PathVariable String email) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado com e-mail: " + email));
        return ResponseEntity.ok(EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).buscarPorEmail(email)).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).buscarPorId(usuario.getId())).withRel("usuario"),
                linkTo(methodOn(UsuarioController.class).listarTodos(null)).withRel("lista")));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado")
    })
    @Operation(summary = "Atualiza dados do usuario", description = "Altera nome ou e-mail de um usuario cadastrado")
    @PutMapping("/{id}")
    public EntityModel<Usuario> atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return repository.findById(id).map(usuario -> {
            usuario.setNome(request.nome());
            usuario.setEmail(request.email());
            return criarModelo(repository.save(usuario));
        }).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    private EntityModel<Usuario> criarModelo(Usuario usuario) {
        return EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).buscarPorId(usuario.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listarTodos(null)).withRel("lista"));
    }
}
