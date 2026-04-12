package com.meuapi.games_api.repositories;

import com.meuapi.games_api.entities.Plataforma;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlataformaRepository extends JpaRepository<Plataforma, Long> {
    List<Plataforma> findByNomeContainingIgnoreCase(String nome);
}
