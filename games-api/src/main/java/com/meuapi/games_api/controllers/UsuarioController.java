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
    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PagedResourcesAssembler<Usuario> pagedResourcesAssembler;

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Usuario>>> listarTodos(Pageable pageable) {
        Page<Usuario> usuarios = repository.findAll(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(usuarios));
    }

    @PostMapping
    public ResponseEntity<Usuario> criar(@Valid @RequestBody Usuario usuario) {
        return ResponseEntity.status(201).body(repository.save(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
