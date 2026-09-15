package br.com.igorpacheco.apinutri.repository;

import br.com.igorpacheco.apinutri.model.AlimentoMacro;
import br.com.igorpacheco.apinutri.model.AlimentoMacroId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlimentoMacroRepository extends JpaRepository<AlimentoMacro, AlimentoMacroId> {

    List<AlimentoMacro> findByAlimento_IdAlimento(Integer idAlimento);
}
