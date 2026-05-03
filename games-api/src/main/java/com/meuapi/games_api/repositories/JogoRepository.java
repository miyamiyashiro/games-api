package com.meuapi.games_api.repositories;

import com.meuapi.games_api.entities.Jogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JogoRepository extends JpaRepository<Jogo, Long> {
    List<Jogo> findByTituloContainingIgnoreCase(String titulo);
}
