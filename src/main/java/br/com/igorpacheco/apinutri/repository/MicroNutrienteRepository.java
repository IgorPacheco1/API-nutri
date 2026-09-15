package br.com.igorpacheco.apinutri.repository;

import br.com.igorpacheco.apinutri.model.MicroNutriente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MicroNutrienteRepository extends JpaRepository<MicroNutriente, Integer> {

    Optional<MicroNutriente> findByNomeMicros(String nomeMicros);
}
