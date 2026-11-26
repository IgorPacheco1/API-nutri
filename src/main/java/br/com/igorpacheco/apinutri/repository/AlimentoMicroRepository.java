package br.com.igorpacheco.apinutri.repository;

import br.com.igorpacheco.apinutri.model.AlimentoMicro;
import br.com.igorpacheco.apinutri.model.AlimentoMicroId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlimentoMicroRepository extends JpaRepository<AlimentoMicro, AlimentoMicroId> {
    List<AlimentoMicro> findByAlimento_IdAlimento(Integer idAlimento);

    /** Valores de um micronutriente (ex.: "Sódio") para todos os alimentos, em uma única consulta. */
    @Query("""
            select new br.com.igorpacheco.apinutri.repository.ValorNutriente(
                am.id.idAlimento, m.nomeMicros, am.valorPor100g)
            from AlimentoMicro am
            join am.microNutriente m
            where m.nomeMicros = :nome
            """)
    List<ValorNutriente> listarValoresDoMicronutriente(@Param("nome") String nome);
}
