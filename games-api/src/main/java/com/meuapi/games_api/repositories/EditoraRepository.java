package com.meuapi.games_api.repositories;

import com.meuapi.games_api.entities.Editora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EditoraRepository extends JpaRepository<Editora, Long> {
    List<Editora> findByNomeContainingIgnoreCase(String nome);
}
