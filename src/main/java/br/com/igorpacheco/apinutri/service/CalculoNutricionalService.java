package br.com.igorpacheco.apinutri.service;

import br.com.igorpacheco.apinutri.dto.CalculoRequestDTO;
import br.com.igorpacheco.apinutri.dto.CalculoResponseDTO;
import br.com.igorpacheco.apinutri.dto.ItemCalculoDTO;
import br.com.igorpacheco.apinutri.dto.MacrosDTO;
import br.com.igorpacheco.apinutri.exception.RecusoNaoEncontradoException;
import br.com.igorpacheco.apinutri.model.Alimento;
import br.com.igorpacheco.apinutri.model.AlimentoMacro;
import br.com.igorpacheco.apinutri.model.AlimentoMicro;
import br.com.igorpacheco.apinutri.model.Nutrientes;
import br.com.igorpacheco.apinutri.repository.AlimentoMacroRepository;
import br.com.igorpacheco.apinutri.repository.AlimentoMicroRepository;
import br.com.igorpacheco.apinutri.repository.AlimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class CalculoNutricionalService {
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final AlimentoRepository alimentoRepository;
    private final AlimentoMacroRepository alimentoMacroRepository;
    private final AlimentoMicroRepository alimentoMicroRepository;

    public CalculoResponseDTO calcular(CalculoRequestDTO request) {
        BigDecimal totalCalorias = BigDecimal.ZERO;
        BigDecimal totalProteinas = BigDecimal.ZERO;
        BigDecimal totalCarboidratos = BigDecimal.ZERO;
        BigDecimal totalGorduras = BigDecimal.ZERO;

        Map<String, BigDecimal> totalMicros = new HashMap<>();
        Map<String, String> unidadesMicros = new TreeMap<>();

        for (ItemCalculoDTO item : request.itens()) {

            Alimento alimento = alimentoRepository.findById(item.idAlimento())
                    .orElseThrow(() -> new RecusoNaoEncontradoException("Não temos esse alimento cadastrado no nosso database: " + item.idAlimento()));

            BigDecimal proporcao = item.quantidadeGramas().divide(CEM);

            if (alimento.getCaloriasPor100g() != null) {
                totalCalorias = totalCalorias.add(alimento.getCaloriasPor100g().multiply(proporcao));
            }

            for (AlimentoMacro am : alimentoMacroRepository.findByAlimento_IdAlimento(item.idAlimento())) {
                if (am.getGramasPor100g() == null) {
                    continue;
                }
                BigDecimal valor = am.getGramasPor100g().multiply(proporcao);
                // os nomes vêm do banco; as constantes garantem a grafia certa ("Lipídeos")
                switch (Nutrientes.normalizar(am.getMacroNutriente().getNomeMacros())) {
                    case Nutrientes.PROTEINA -> totalProteinas = totalProteinas.add(valor);
                    case Nutrientes.CARBOIDRATO -> totalCarboidratos = totalCarboidratos.add(valor);
                    case Nutrientes.LIPIDEOS -> totalGorduras = totalGorduras.add(valor);
                    default -> {
                        // macronutriente desconhecido: ignorado
                    }
                }
            }

            for (AlimentoMicro amic : alimentoMicroRepository.findByAlimento_IdAlimento(item.idAlimento())) {
                if (amic.getValorPor100g() == null) {
                    continue;
                }
                String nomeMicro = amic.getMicroNutriente().getNomeMicros();
                BigDecimal valor = amic.getValorPor100g().multiply(proporcao);
                totalMicros.merge(nomeMicro, valor, BigDecimal::add);
                if (amic.getMicroNutriente().getUnidade() != null) {
                    unidadesMicros.put(nomeMicro, amic.getMicroNutriente().getUnidade());
                }
            }
        }

        return new CalculoResponseDTO(totalCalorias,
                new MacrosDTO(totalProteinas, totalCarboidratos, totalGorduras),
                totalMicros,
                unidadesMicros);
    }
}
