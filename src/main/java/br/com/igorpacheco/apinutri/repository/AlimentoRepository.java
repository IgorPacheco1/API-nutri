package br.com.igorpacheco.apinutri.repository;

import br.com.igorpacheco.apinutri.model.Alimento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlimentoRepository extends JpaRepository<Alimento, Integer> {

    /** Todos os alimentos, já com a categoria carregada (uma única consulta). */
    @EntityGraph(attributePaths = "categoria")
    List<Alimento> findAllByOrderByIdAlimentoAsc();

    /** Usado para completar a categoria dos alimentos importados antes da versão 1.1.0. */
    List<Alimento> findByCategoriaIsNull();
}
