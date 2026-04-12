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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@Tag(name = "Empréstimos")
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoRepository repository;
    private final PagedResourcesAssembler<Emprestimo> assembler;

    @Autowired
    public EmprestimoController(EmprestimoRepository repository, PagedResourcesAssembler<Emprestimo> assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Busca empréstimos por data específica", description = "Busca personalizda onde pode buscar empréstimos por data")
    @GetMapping("/data")
    public ResponseEntity<?> buscarPorData(@RequestParam LocalDate data) {
        return ResponseEntity.ok(repository.findByDataEmprestimo(data));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Lista todos os empréstimos", description = "Retorna uma lista paginada com links HATEOAS")
    @GetMapping
    public PagedModel<EntityModel<Emprestimo>> listarTodos(Pageable pageable) {
        Page<Emprestimo> emprestimos = repository.findAll(pageable);
        return assembler.toModel(emprestimos,
                emp -> EntityModel.of(emp,
                        linkTo(methodOn(EmprestimoController.class).buscarPorId(emp.getId())).withSelfRel()));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Busca um empréstimo por ID", description = "Retorna os detalhes de um empréstimo específico")
    @GetMapping("/{id}")
    public EntityModel<Emprestimo> buscarPorId(@PathVariable Long id) {
        Emprestimo emp = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado"));
        return EntityModel.of(emp,
                linkTo(methodOn(EmprestimoController.class).buscarPorId(id)).withSelfRel());
    }

    @Operation(summary = "Registra um novo empréstimo",
            description = "Cria um vínculo entre um usuário e um jogo, definindo a data de saída automaticamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empréstimo registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou falta de dados obrigatórios"),
            @ApiResponse(responseCode = "404", description = "O Usuário ou o Jogo informado para o empréstimo não foi encontrado no sistema"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao processar a transação")
    })
    @PostMapping
    public ResponseEntity<Emprestimo> realizarEmprestimo(@Valid @RequestBody Emprestimo emprestimo) {
        emprestimo.setDataEmprestimo(LocalDate.now());
        return ResponseEntity.status(201).body(repository.save(emprestimo));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Atualiza um empréstimo", description = "Permite alterar um empréstimo já existente")
    @PutMapping("/{id}")
    public EntityModel<Emprestimo> atualizar(@PathVariable Long id, @RequestBody Emprestimo novo) {
        return repository.findById(id).map(emp -> {
            emp.setDataDevolucao(novo.getDataDevolucao());
            return EntityModel.of(repository.save(emp),
                    linkTo(methodOn(EmprestimoController.class).buscarPorId(id)).withSelfRel());
        }).orElseThrow(() -> new RuntimeException("Não encontrado"));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Empréstimo excluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Operation(summary = "Exclui um empréstimo", description = "Remove permanentemente o empréstimo do acervo")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}