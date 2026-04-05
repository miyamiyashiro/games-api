package com.meuapi.games_api.infrastructure;

import com.meuapi.games_api.entities.Jogo;
import com.meuapi.games_api.entities.Categoria;
import com.meuapi.games_api.repositories.JogoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class LoadDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(JogoRepository repository) {
        return args -> {
            log.info("Preloading " + repository.save(new Jogo("Catan", Categoria.TABULEIRO)));
            log.info("Preloading " + repository.save(new Jogo("Dungeons & Dragons", Categoria.RPG)));
        };
    }
}
