package br.com.igorpacheco.apinutri.repository;

import br.com.igorpacheco.apinutri.model.MacroNutriente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MacroNutrienteRepository extends JpaRepository<MacroNutriente, Integer> {
}
