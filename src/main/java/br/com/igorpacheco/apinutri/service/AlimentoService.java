package br.com.igorpacheco.apinutri.service;

import br.com.igorpacheco.apinutri.dto.AlimentoResumoDTO;
import br.com.igorpacheco.apinutri.repository.AlimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlimentoService {
    private final AlimentoRepository alimentoRepository;

    public List<AlimentoResumoDTO> buscarPorNome(String nome) {
        return alimentoRepository.findByNomeAlimentoContainingIgnoreCase(nome)
                .stream()
                .map(a -> new AlimentoResumoDTO(a.getIdAlimento(), a.getNomeAlimento()))
                .toList();
    }
}
