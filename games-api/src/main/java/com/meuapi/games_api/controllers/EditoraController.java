package com.meuapi.games_api.controllers;

import com.meuapi.games_api.entities.Editora;
import com.meuapi.games_api.entities.Editora;
import com.meuapi.games_api.repositories.EditoraRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public class EditoraController {
    @Autowired
    private EditoraRepository repository;

    @Autowired
    private PagedResourcesAssembler<Editora> pagedResourcesAssembler;

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Editora>>> listarTodos(Pageable pageable) {
        Page<Editora> usuarios = repository.findAll(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(usuarios));
    }

    @PostMapping
    public ResponseEntity<Editora> criar(@Valid @RequestBody Editora usuario) {
        return ResponseEntity.status(201).body(repository.save(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
