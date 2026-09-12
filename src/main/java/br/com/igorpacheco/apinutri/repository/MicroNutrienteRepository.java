package br.com.igorpacheco.apinutri.repository;

import br.com.igorpacheco.apinutri.model.MicroNutriente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MicroNutrienteRepository extends JpaRepository<MicroNutriente, Integer> {
}
