package br.com.igorpacheco.apinutri.repository;

import br.com.igorpacheco.apinutri.model.Alimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlimentoRepository extends JpaRepository<Alimento, Integer> {
    List<Alimento> findByAlimentoContainingIgnoreCase(String alimento);
}
