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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository repository;
    private final PagedResourcesAssembler<Usuario> pagedResourcesAssembler;

    @Autowired
    public UsuarioController(UsuarioRepository repository, PagedResourcesAssembler<Usuario> pagedResourcesAssembler) {
        this.repository = repository;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Usuario>>> listarTodos(Pageable pageable) {
        Page<Usuario> usuarios = repository.findAll(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(usuarios));
    }

    @PostMapping
    public ResponseEntity<Usuario> criar(@Valid @RequestBody Usuario usuario) {
        return ResponseEntity.status(201).body(repository.save(usuario));
    }

    @GetMapping("/{id}")
    public EntityModel<Usuario> buscarPorId(@PathVariable Long id) {
        Usuario usuario = repository.findById(id).orElseThrow();
        return EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).buscarPorId(id)).withSelfRel());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}