package com.meuapi.games_api.infrastructure;

import com.meuapi.games_api.entities.Categoria;
import com.meuapi.games_api.entities.DetalhesJogo;
import com.meuapi.games_api.entities.Editora;
import com.meuapi.games_api.entities.Jogo;
import com.meuapi.games_api.entities.Plataforma;
import com.meuapi.games_api.repositories.DetalhesJogoRepository;
import com.meuapi.games_api.repositories.EditoraRepository;
import com.meuapi.games_api.repositories.JogoRepository;
import com.meuapi.games_api.repositories.PlataformaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LoadDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(
            JogoRepository jogoRepository,
            EditoraRepository editoraRepository,
            PlataformaRepository plataformaRepository,
            DetalhesJogoRepository detalhesJogoRepository
    ) {
        return args -> {
            Editora galapagos = editoraRepository.save(criarEditora("Galapagos Jogos"));
            Plataforma tabuleiro = plataformaRepository.save(criarPlataforma("Tabuleiro fisico"));
            Plataforma rpg = plataformaRepository.save(criarPlataforma("RPG de mesa"));

            Jogo catan = new Jogo();
            catan.setTitulo("Catan");
            catan.setCategoria(Categoria.TABULEIRO);
            catan.setEditora(galapagos);
            catan.setPlataformas(List.of(tabuleiro));
            catan = jogoRepository.save(catan);

            Jogo dnd = new Jogo();
            dnd.setTitulo("Dungeons & Dragons");
            dnd.setCategoria(Categoria.RPG);
            dnd.setEditora(galapagos);
            dnd.setPlataformas(List.of(rpg));
            dnd = jogoRepository.save(dnd);

            detalhesJogoRepository.save(criarDetalhes("Jogo de negociacao e estrategia.", 10, 90, catan));
            detalhesJogoRepository.save(criarDetalhes("Sistema de RPG de fantasia medieval.", 12, 180, dnd));

            log.info("Base inicial de jogos carregada com sucesso");
        };
    }

    private Editora criarEditora(String nome) {
        Editora editora = new Editora();
        editora.setNome(nome);
        return editora;
    }

    private Plataforma criarPlataforma(String nome) {
        Plataforma plataforma = new Plataforma();
        plataforma.setNome(nome);
        return plataforma;
    }

    private DetalhesJogo criarDetalhes(String descricao, Integer idadeMinima, Integer tempoMedioMinutos, Jogo jogo) {
        DetalhesJogo detalhes = new DetalhesJogo();
        detalhes.setDescricao(descricao);
        detalhes.setIdadeMinima(idadeMinima);
        detalhes.setTempoMedioMinutos(tempoMedioMinutos);
        detalhes.setJogo(jogo);
        return detalhes;
    }
}
