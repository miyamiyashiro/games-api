package com.meuapi.games_api.controllers;

import com.meuapi.games_api.entities.Jogo;
import com.meuapi.games_api.repositories.JogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import java.util.List;
import java.util.stream.Collectors;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/jogos")
public class JogoController {

    private final JogoRepository repository;
    private final PagedResourcesAssembler<Jogo> pagedResourcesAssembler;

    @Autowired
    public JogoController(JogoRepository repository, PagedResourcesAssembler<Jogo> pagedResourcesAssembler) {
        this.repository = repository;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @GetMapping
    public PagedModel<EntityModel<Jogo>> listarTodos(Pageable pageable) {
        Page<Jogo> jogos = repository.findAll(pageable);
        return pagedResourcesAssembler.toModel(jogos,
                jogo -> EntityModel.of(jogo,
                        linkTo(methodOn(JogoController.class).buscarPorId(jogo.getId())).withSelfRel()));
    }

    @GetMapping("/{id}")
    public EntityModel<Jogo> buscarPorId(@PathVariable Long id) {
        Jogo jogo = repository.findById(id).orElseThrow();
        return EntityModel.of(jogo,
                linkTo(methodOn(JogoController.class).buscarPorId(id)).withSelfRel());
    }

    @PutMapping("/{id}")
    public EntityModel<Jogo> atualizar(@PathVariable Long id, @RequestBody Jogo novoJogo) {
        return repository.findById(id)
                .map(jogo -> {
                    jogo.setTitulo(novoJogo.getTitulo());
                    jogo.setCategoria(novoJogo.getCategoria());
                    Jogo atualizado = repository.save(jogo);
                    return EntityModel.of(atualizado,
                            linkTo(methodOn(JogoController.class).buscarPorId(id)).withSelfRel());
                })
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/busca")
    public CollectionModel<EntityModel<Jogo>> buscarPorTitulo(@RequestParam String titulo) {
        List<EntityModel<Jogo>> jogos = repository.findByTituloContainingIgnoreCase(titulo).stream()
                .map(jogo -> EntityModel.of(jogo,
                        linkTo(methodOn(JogoController.class).buscarPorId(jogo.getId())).withSelfRel()))
                .collect(Collectors.toList());
        return CollectionModel.of(jogos);
    }
}