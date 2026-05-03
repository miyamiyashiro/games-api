package com.meuapi.games_api.controllers;

import com.meuapi.games_api.dto.EmprestimoRequest;
import com.meuapi.games_api.entities.Emprestimo;
import com.meuapi.games_api.entities.Jogo;
import com.meuapi.games_api.entities.Usuario;
import com.meuapi.games_api.exceptions.RecursoNaoEncontradoException;
import com.meuapi.games_api.repositories.EmprestimoRepository;
import com.meuapi.games_api.repositories.JogoRepository;
import com.meuapi.games_api.repositories.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@Tag(name = "Emprestimos")
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final JogoRepository jogoRepository;
    private final PagedResourcesAssembler<Emprestimo> assembler;

    @Autowired
    public EmprestimoController(
            EmprestimoRepository repository,
            UsuarioRepository usuarioRepository,
            JogoRepository jogoRepository,
            PagedResourcesAssembler<Emprestimo> assembler
    ) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.jogoRepository = jogoRepository;
        this.assembler = assembler;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emprestimos listados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos")
    })
    @Operation(summary = "Lista todos os emprestimos", description = "Retorna uma lista paginada com links HATEOAS")
    @GetMapping
    public PagedModel<EntityModel<Emprestimo>> listarTodos(Pageable pageable) {
        Page<Emprestimo> emprestimos = repository.findAll(pageable);
        return assembler.toModel(emprestimos, this::criarModelo);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emprestimo encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Emprestimo nao encontrado")
    })
    @Operation(summary = "Busca um emprestimo por ID", description = "Retorna os detalhes de um emprestimo especifico")
    @GetMapping("/{id}")
    public EntityModel<Emprestimo> buscarPorId(@PathVariable Long id) {
        Emprestimo emprestimo = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
        return criarModelo(emprestimo);
    }

    @Operation(summary = "Registra um novo emprestimo", description = "Cria um vinculo entre um usuario e um jogo usando seus IDs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Emprestimo registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validacao ou falta de dados obrigatorios")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Emprestimo>> realizarEmprestimo(@Valid @RequestBody EmprestimoRequest request) {
        Emprestimo novo = new Emprestimo();
        preencherEmprestimo(novo, request);
        if (novo.getDataEmprestimo() == null) {
            novo.setDataEmprestimo(LocalDate.now());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(criarModelo(repository.save(novo)));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emprestimo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Emprestimo nao encontrado")
    })
    @Operation(summary = "Atualiza um emprestimo", description = "Permite alterar as datas e os vinculos do emprestimo")
    @PutMapping("/{id}")
    public EntityModel<Emprestimo> atualizar(@PathVariable Long id, @Valid @RequestBody EmprestimoRequest request) {
        return repository.findById(id).map(emprestimo -> {
            preencherEmprestimo(emprestimo, request);
            return criarModelo(repository.save(emprestimo));
        }).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Emprestimo excluido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Emprestimo nao encontrado")
    })
    @Operation(summary = "Exclui um emprestimo", description = "Remove permanentemente o emprestimo do acervo")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException(id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parametro invalido")
    })
    @Operation(summary = "Busca emprestimos por data", description = "Consulta personalizada pela data de emprestimo")
    @GetMapping("/data")
    public CollectionModel<EntityModel<Emprestimo>> buscarPorData(@RequestParam LocalDate data) {
        List<EntityModel<Emprestimo>> emprestimos = repository.findByDataEmprestimo(data).stream()
                .map(this::criarModelo)
                .toList();
        return CollectionModel.of(emprestimos,
                linkTo(methodOn(EmprestimoController.class).buscarPorData(data)).withSelfRel(),
                linkTo(methodOn(EmprestimoController.class).listarTodos(null)).withRel("lista"));
    }

    private void preencherEmprestimo(Emprestimo emprestimo, EmprestimoRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado com ID: " + request.usuarioId()));
        Jogo jogo = jogoRepository.findById(request.jogoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo nao encontrado com ID: " + request.jogoId()));

        emprestimo.setDataEmprestimo(request.dataEmprestimo());
        emprestimo.setDataDevolucao(request.dataDevolucao());
        emprestimo.setUsuario(usuario);
        emprestimo.setJogo(jogo);
    }

    private EntityModel<Emprestimo> criarModelo(Emprestimo emprestimo) {
        return EntityModel.of(emprestimo,
                linkTo(methodOn(EmprestimoController.class).buscarPorId(emprestimo.getId())).withSelfRel(),
                linkTo(methodOn(EmprestimoController.class).listarTodos(null)).withRel("lista"));
    }
}
