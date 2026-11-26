package br.com.igorpacheco.apinutri.repository;

import br.com.igorpacheco.apinutri.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    Optional<Categoria> findByNomeCategoria(String nomeCategoria);
}
