package br.com.igorpacheco.apinutri.repository;

import br.com.igorpacheco.apinutri.model.AlimentoMacro;
import br.com.igorpacheco.apinutri.model.AlimentoMacroId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlimentoMacroRepository extends JpaRepository<AlimentoMacro, AlimentoMacroId> {

    List<AlimentoMacro> findByAlimento_IdAlimento(Integer idAlimento);

    /** Todos os macronutrientes de todos os alimentos, em uma única consulta. */
    @Query("""
            select new br.com.igorpacheco.apinutri.repository.ValorNutriente(
                am.id.idAlimento, m.nomeMacros, am.gramasPor100g)
            from AlimentoMacro am
            join am.macroNutriente m
            """)
    List<ValorNutriente> listarValoresDeMacros();
}
