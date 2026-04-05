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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/jogos")

public class JogoController {
    @Autowired
    private JogoRepository repository;

    @Autowired
    private PagedResourcesAssembler<Jogo> pagedResourcesAssembler;

    @GetMapping
    public PagedModel<EntityModel<Jogo>> listarTodos(Pageable pageable) {
        Page<Jogo> jogos = repository.findAll(pageable);
        // Retorna os dados com links HATEOAS e paginação automática
        return pagedResourcesAssembler.toModel(jogos,
                jogo -> EntityModel.of(jogo,
                        linkTo(methodOn(JogoController.class).buscarPorId(jogo.getId())).withSelfRel()));
    }

    @GetMapping("/{id}")
    public EntityModel<Jogo> buscarPorId(@PathVariable Long id) {
        Jogo jogo = repository.findById(id).orElseThrow();
        return EntityModel.of(jogo,
                linkTo(methodOn(JogoController.class).buscarPorId(id)).withSelfRel(),
                linkTo(methodOn(JogoController.class).listarTodos(null)).withRel("lista"));
    }
}
