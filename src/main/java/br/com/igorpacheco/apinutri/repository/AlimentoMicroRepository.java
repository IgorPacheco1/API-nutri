package br.com.igorpacheco.apinutri.repository;

import br.com.igorpacheco.apinutri.model.Alimento;
import br.com.igorpacheco.apinutri.model.AlimentoMicro;
import br.com.igorpacheco.apinutri.model.AlimentoMicroId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlimentoMicroRepository extends JpaRepository<AlimentoMicro, AlimentoMicroId> {
    List<AlimentoMicro> findByAlimento_IdAlimento(Integer idAlimento);
}
