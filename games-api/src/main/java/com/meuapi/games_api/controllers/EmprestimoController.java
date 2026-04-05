package com.meuapi.games_api.controllers;

import com.meuapi.games_api.entities.Emprestimo;
import com.meuapi.games_api.repositories.EmprestimoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;


@RestController
@RequestMapping("/emprestimos")

public class EmprestimoController {
    private final EmprestimoRepository repository;
    private final PagedResourcesAssembler<Emprestimo> assembler;

    @Autowired
    public EmprestimoController(EmprestimoRepository repository, PagedResourcesAssembler<Emprestimo> assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Emprestimo>>> listar(Pageable pageable) {
        Page<Emprestimo> emprestimos = repository.findAll(pageable);
        return ResponseEntity.ok(assembler.toModel(emprestimos));
    }

    @PostMapping
    public ResponseEntity<Emprestimo> realizarEmprestimo(@Valid @RequestBody Emprestimo emprestimo) {
        // Define a data atual como data do empréstimo automaticamente
        emprestimo.setDataEmprestimo(LocalDate.now());
        return ResponseEntity.status(201).body(repository.save(emprestimo));
    }

    @GetMapping("/{id}")
    public EntityModel<Emprestimo> buscar(@PathVariable Long id) {
        Emprestimo emprestimo = repository.findById(id).orElseThrow();
        return EntityModel.of(emprestimo,
                linkTo(methodOn(EmprestimoController.class).buscar(id)).withSelfRel(),
                linkTo(methodOn(EmprestimoController.class).listar(null)).withRel("todos-emprestimos"));
    }
}
