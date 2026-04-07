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
    public PagedModel<EntityModel<Emprestimo>> listarTodos(Pageable pageable) {
        Page<Emprestimo> emprestimos = repository.findAll(pageable);
        return assembler.toModel(emprestimos,
                emp -> EntityModel.of(emp,
                        linkTo(methodOn(EmprestimoController.class).buscarPorId(emp.getId())).withSelfRel()));
    }

    @GetMapping("/{id}")
    public EntityModel<Emprestimo> buscarPorId(@PathVariable Long id) {
        Emprestimo emp = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado"));
        return EntityModel.of(emp,
                linkTo(methodOn(EmprestimoController.class).buscarPorId(id)).withSelfRel());
    }

    @PostMapping
    public ResponseEntity<Emprestimo> realizarEmprestimo(@Valid @RequestBody Emprestimo emprestimo) {
        emprestimo.setDataEmprestimo(LocalDate.now());
        return ResponseEntity.status(201).body(repository.save(emprestimo));
    }

    @PutMapping("/{id}")
    public EntityModel<Emprestimo> atualizar(@PathVariable Long id, @RequestBody Emprestimo novo) {
        return repository.findById(id).map(emp -> {
            emp.setDataDevolucao(novo.getDataDevolucao());
            return EntityModel.of(repository.save(emp),
                    linkTo(methodOn(EmprestimoController.class).buscarPorId(id)).withSelfRel());
        }).orElseThrow(() -> new RuntimeException("Não encontrado"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}