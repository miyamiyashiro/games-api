package com.meuapi.games_api.repositories;

import com.meuapi.games_api.entities.DetalhesJogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DetalhesJogoRepository extends JpaRepository<DetalhesJogo, Long> {
    Optional<DetalhesJogo> findByJogoId(Long jogoId);
}
