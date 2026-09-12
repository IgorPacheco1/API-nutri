package br.com.igorpacheco.apinutri.repository;

import br.com.igorpacheco.apinutri.model.Alimento;
import br.com.igorpacheco.apinutri.model.AlimentoMicro;
import br.com.igorpacheco.apinutri.model.AlimentoMicroId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlimentoMicroRepository extends JpaRepository<AlimentoMicro, AlimentoMicroId> {
}
