package br.com.igorpacheco.apinutri.service;

import br.com.igorpacheco.apinutri.dto.CalculoRequestDTO;
import br.com.igorpacheco.apinutri.dto.CalculoResponseDTO;
import br.com.igorpacheco.apinutri.dto.ItemCalculoDTO;
import br.com.igorpacheco.apinutri.dto.MacrosDTO;
import br.com.igorpacheco.apinutri.exception.RecusoNaoEncontradoException;
import br.com.igorpacheco.apinutri.model.Alimento;
import br.com.igorpacheco.apinutri.model.AlimentoMacro;
import br.com.igorpacheco.apinutri.model.AlimentoMicro;
import br.com.igorpacheco.apinutri.repository.AlimentoMacroRepository;
import br.com.igorpacheco.apinutri.repository.AlimentoMicroRepository;
import br.com.igorpacheco.apinutri.repository.AlimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CalculoNutricionalService {
    private final AlimentoRepository alimentoRepository;
    private final AlimentoMacroRepository alimentoMacroRepository;
    private final AlimentoMicroRepository alimentoMicroRepository;

    public CalculoResponseDTO calcular(CalculoRequestDTO request) {
        BigDecimal totalCalorias = BigDecimal.ZERO;
        BigDecimal totalProteinas = BigDecimal.ZERO;
        BigDecimal totalCarboidratos = BigDecimal.ZERO;
        BigDecimal totalGorduras = BigDecimal.ZERO;

        Map<String, BigDecimal> totalMicros = new HashMap<>(); //problema está na quantidade de argumentos passados

        for (ItemCalculoDTO item : request.itens()) {

            Alimento alimento = alimentoRepository.findById(item.idAlimento())
                    .orElseThrow(() -> new RecusoNaoEncontradoException("Não temos esse alimento cadastrado no nosso database: " + item.idAlimento()));

            BigDecimal proporcao = item.quantidadeGramas().divide(BigDecimal.valueOf(100));

            if (alimento.getCaloriasPor100g() != null) {
                totalCalorias = totalCalorias.add(alimento.getCaloriasPor100g().multiply(proporcao));
            }

            for (AlimentoMacro am : alimentoMacroRepository.findByAlimento_IdAlimento(item.idAlimento())) {
                BigDecimal valor = am.getGramasPor100g().multiply(proporcao);
                switch (am.getMacroNutriente().getNomeMacros()) {
                    case "Proteína" -> totalProteinas = totalProteinas.add(valor);
                    case "Carboidrato" -> totalCarboidratos = totalCarboidratos.add(valor);
                    case "Lipídios" -> totalGorduras = totalGorduras.add(valor);
                }
            }

            for (AlimentoMicro amic : alimentoMicroRepository.findByAlimento_IdAlimento(item.idAlimento())) {
                BigDecimal valor = amic.getGramasPor100g().multiply(proporcao);
                totalMicros.merge(amic.getMicroNutriente().getNomeMicros(), valor, BigDecimal::add);
            }
        }

        return new CalculoResponseDTO(totalCalorias, new MacrosDTO(totalProteinas, totalCarboidratos, totalGorduras), totalMicros
        );
    }
}
