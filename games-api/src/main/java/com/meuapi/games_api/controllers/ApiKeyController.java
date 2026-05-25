package com.meuapi.games_api.controllers;

import com.meuapi.games_api.dto.ApiKeyRequest;
import com.meuapi.games_api.dto.ApiKeyResponse;
import com.meuapi.games_api.entities.Usuario;
import com.meuapi.games_api.exceptions.RecursoNaoEncontradoException;
import com.meuapi.games_api.repositories.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@Tag(name = "Autenticacao - API Keys")
@RequestMapping("/api-keys")
public class ApiKeyController {

    private final UsuarioRepository usuarioRepository;

    public ApiKeyController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chaves listadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou invalida")
    })
    @Operation(summary = "Lista chaves de API", description = "Lista usuarios que possuem chave ativa, exibindo a chave mascarada")
    @GetMapping
    public CollectionModel<EntityModel<ApiKeyResponse>> listar() {
        var chaves = usuarioRepository.findAllByApiKeyIsNotNull().stream()
                .map(usuario -> criarModelo(usuario, true))
                .toList();

        return CollectionModel.of(chaves, linkTo(methodOn(ApiKeyController.class).listar()).withSelfRel());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chave encontrada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou invalida"),
            @ApiResponse(responseCode = "404", description = "Chave de API nao encontrada")
    })
    @Operation(summary = "Busca chave de API por ID", description = "Busca a chave vinculada ao usuario informado. O ID usado e o ID do usuario dono da chave.")
    @GetMapping("/{id}")
    public EntityModel<ApiKeyResponse> buscarPorId(@PathVariable Long id) {
        Usuario usuario = buscarUsuarioComChave(id);
        return criarModelo(usuario, true);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chave de API gerada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado")
    })
    @Operation(summary = "Gera chave de API", description = "Cria ou renova a chave usada no header X-API-Key para um usuario")
    @PostMapping
    public ResponseEntity<EntityModel<ApiKeyResponse>> gerar(@Valid @RequestBody ApiKeyRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(request.usuarioId()));

        usuario.setApiKey(UUID.randomUUID().toString());
        Usuario salvo = usuarioRepository.save(usuario);

        return ResponseEntity.ok(criarModelo(salvo, false));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Chave revogada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente ou invalida"),
            @ApiResponse(responseCode = "404", description = "Chave de API nao encontrada")
    })
    @Operation(summary = "Revoga chave de API", description = "Remove a chave vinculada ao usuario informado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revogar(@PathVariable Long id) {
        Usuario usuario = buscarUsuarioComChave(id);
        usuario.setApiKey(null);
        usuarioRepository.save(usuario);
        return ResponseEntity.noContent().build();
    }

    private Usuario buscarUsuarioComChave(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));

        if (usuario.getApiKey() == null || usuario.getApiKey().isBlank()) {
            throw new RecursoNaoEncontradoException("Chave de API nao encontrada para o usuario ID: " + id);
        }

        return usuario;
    }

    private EntityModel<ApiKeyResponse> criarModelo(Usuario usuario, boolean mascararChave) {
        ApiKeyResponse response = new ApiKeyResponse(
                usuario.getId(),
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                mascararChave ? mascarar(usuario.getApiKey()) : usuario.getApiKey(),
                "ATIVA"
        );

        return EntityModel.of(response,
                linkTo(methodOn(ApiKeyController.class).buscarPorId(usuario.getId())).withSelfRel(),
                linkTo(methodOn(ApiKeyController.class).listar()).withRel("lista"),
                linkTo(methodOn(UsuarioController.class).buscarPorId(usuario.getId())).withRel("usuario"));
    }

    private String mascarar(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "********";
        }

        return apiKey.substring(0, 4) + "********" + apiKey.substring(apiKey.length() - 4);
    }
}
