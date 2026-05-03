package com.meuapi.games_api.controllers;

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
            @ApiResponse(responseCode = "200", description = "Usuários listados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    @Operation(summary = "Lista todos os usuários", description = "Retorna uma lista paginada com links HATEOAS")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Usuario>>> listarTodos(Pageable pageable) {
        Page<Usuario> usuarios = repository.findAll(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(usuarios, this::criarModelo));
    }

    @Operation(summary = "Cadastra um novo usuário", description = "Cria um perfil de cliente para realizar empréstimos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já cadastrado")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Usuario>> criar(@Valid @RequestBody Usuario usuario) {
        Usuario novo = repository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(criarModelo(novo));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @Operation(summary = "Busca usuário por ID", description = "Retorna os detalhes de um usuário específico")
    @GetMapping("/{id}")
    public EntityModel<Usuario> buscarPorId(@PathVariable Long id) {
        Usuario usuario = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
        return criarModelo(usuario);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @Operation(summary = "Deleta usuário", description = "Remove permanentemente o usuário do acervo")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException(id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @Operation(summary = "Busca usuário por e-mail", description = "Consulta personalizada por e-mail exato")
    @GetMapping("/email/{email}")
    public ResponseEntity<EntityModel<Usuario>> buscarPorEmail(@PathVariable String email) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com e-mail: " + email));
        return ResponseEntity.ok(EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).buscarPorEmail(email)).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).buscarPorId(usuario.getId())).withRel("usuario"),
                linkTo(methodOn(UsuarioController.class).listarTodos(null)).withRel("lista")));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @Operation(summary = "Atualiza dados do usuário", description = "Altera nome ou e-mail de um usuário cadastrado")
    @PutMapping("/{id}")
    public EntityModel<Usuario> atualizar(@PathVariable Long id, @Valid @RequestBody Usuario novo) {
        return repository.findById(id).map(usuario -> {
            usuario.setNome(novo.getNome());
            usuario.setEmail(novo.getEmail());
            return criarModelo(repository.save(usuario));
        }).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    private EntityModel<Usuario> criarModelo(Usuario usuario) {
        return EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).buscarPorId(usuario.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listarTodos(null)).withRel("lista"));
    }
}
