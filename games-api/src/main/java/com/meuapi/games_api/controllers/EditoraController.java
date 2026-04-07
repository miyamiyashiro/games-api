package com.meuapi.games_api.controllers;

import com.meuapi.games_api.entities.Editora;
import com.meuapi.games_api.repositories.EditoraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/editoras")
public class EditoraController {

    private final EditoraRepository repository;
    private final PagedResourcesAssembler<Editora> assembler;

    @Autowired
    public EditoraController(EditoraRepository repository, PagedResourcesAssembler<Editora> assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    @GetMapping
    public PagedModel<EntityModel<Editora>> listarTodas(Pageable pageable) {
        Page<Editora> editoras = repository.findAll(pageable);
        return assembler.toModel(editoras,
                e -> EntityModel.of(e, linkTo(methodOn(EditoraController.class).buscarPorId(e.getId())).withSelfRel()));
    }

    @GetMapping("/{id}")
    public EntityModel<Editora> buscarPorId(@PathVariable Long id) {
        Editora editora = repository.findById(id).orElseThrow();
        return EntityModel.of(editora, linkTo(methodOn(EditoraController.class).buscarPorId(id)).withSelfRel());
    }

    @PostMapping
    public ResponseEntity<EntityModel<Editora>> criar(@RequestBody Editora editora) {
        Editora nova = repository.save(editora);
        EntityModel<Editora> model = EntityModel.of(nova,
                linkTo(methodOn(EditoraController.class).buscarPorId(nova.getId())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
