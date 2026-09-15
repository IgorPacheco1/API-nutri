package br.com.igorpacheco.apinutri.service;

import br.com.igorpacheco.apinutri.dto.CalculoRequestDTO;
import br.com.igorpacheco.apinutri.dto.CalculoResponseDTO;
import br.com.igorpacheco.apinutri.dto.ItemCalculoDTO;
import br.com.igorpacheco.apinutri.model.*;
import br.com.igorpacheco.apinutri.repository.AlimentoMacroRepository;
import br.com.igorpacheco.apinutri.repository.AlimentoMicroRepository;
import br.com.igorpacheco.apinutri.repository.AlimentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculoNutricionalServiceTest {

    @Mock
    private AlimentoRepository alimentoRepository;

    @Mock
    private AlimentoMacroRepository alimentoMacroRepository;

    @Mock
    private AlimentoMicroRepository alimentoMicroRepository;

    @InjectMocks
    private CalculoNutricionalService service;

    @Test
    void deveCalcularCaloriasProporcionalmenteAQuantidade() {
        Alimento arroz = new Alimento();
        arroz.setIdAlimento(1);
        arroz.setCaloriasPor100g(BigDecimal.valueOf(124));

        when(alimentoRepository.findById(1)).thenReturn(Optional.of(arroz));
        when(alimentoMacroRepository.findByAlimento_IdAlimento(1)).thenReturn(List.of());
        when(alimentoMicroRepository.findByAlimento_IdAlimento(1)).thenReturn(List.of());

        CalculoRequestDTO request = new CalculoRequestDTO(
                List.of(new ItemCalculoDTO(1, BigDecimal.valueOf(150)))
        );

        CalculoResponseDTO resultado = service.calcular(request);

        assertEquals(0, BigDecimal.valueOf(186.000).compareTo(resultado.totalCalorias()));
    }

    @Test
    void deveDistribuirMacrosNosCamposCorretos() {
        Alimento feijao = new Alimento();
        feijao.setIdAlimento(2);
        feijao.setCaloriasPor100g(BigDecimal.valueOf(76));

        MacroNutriente proteina = new MacroNutriente();
        proteina.setNomeMacros("Proteína");
        AlimentoMacro macroProteina = new AlimentoMacro();
        macroProteina.setMacroNutriente(proteina);
        macroProteina.setGramasPor100g(BigDecimal.valueOf(4.8));

        MacroNutriente carboidrato = new MacroNutriente();
        carboidrato.setNomeMacros("Carboidrato");
        AlimentoMacro macroCarbo = new AlimentoMacro();
        macroCarbo.setMacroNutriente(carboidrato);
        macroCarbo.setGramasPor100g(BigDecimal.valueOf(13.6));

        when(alimentoRepository.findById(2)).thenReturn(Optional.of(feijao));
        when(alimentoMacroRepository.findByAlimento_IdAlimento(2))
                .thenReturn(List.of(macroProteina, macroCarbo));
        when(alimentoMicroRepository.findByAlimento_IdAlimento(2)).thenReturn(List.of());

        CalculoRequestDTO request = new CalculoRequestDTO(
                List.of(new ItemCalculoDTO(2, BigDecimal.valueOf(100)))
        );

        CalculoResponseDTO resultado = service.calcular(request);

        assertEquals(0, BigDecimal.valueOf(4.8).compareTo(resultado.macros().proteinas()));
        assertEquals(0, BigDecimal.valueOf(13.6).compareTo(resultado.macros().carboidratos()));
        assertEquals(0, BigDecimal.ZERO.compareTo(resultado.macros().gorduras()));
    }

    @Test
    void deveSomarTotaisDeMultiplosAlimentos() {
        Alimento arroz = new Alimento();
        arroz.setIdAlimento(1);
        arroz.setCaloriasPor100g(BigDecimal.valueOf(124));

        Alimento feijao = new Alimento();
        feijao.setIdAlimento(2);
        feijao.setCaloriasPor100g(BigDecimal.valueOf(76));

        when(alimentoRepository.findById(1)).thenReturn(Optional.of(arroz));
        when(alimentoRepository.findById(2)).thenReturn(Optional.of(feijao));
        when(alimentoMacroRepository.findByAlimento_IdAlimento(1)).thenReturn(List.of());
        when(alimentoMacroRepository.findByAlimento_IdAlimento(2)).thenReturn(List.of());
        when(alimentoMicroRepository.findByAlimento_IdAlimento(1)).thenReturn(List.of());
        when(alimentoMicroRepository.findByAlimento_IdAlimento(2)).thenReturn(List.of());

        CalculoRequestDTO request = new CalculoRequestDTO(
                List.of(
                        new ItemCalculoDTO(1, BigDecimal.valueOf(100)),
                        new ItemCalculoDTO(2, BigDecimal.valueOf(100))
                )
        );

        CalculoResponseDTO resultado = service.calcular(request);

        // 124 (arroz, 100g) + 76 (feijão, 100g) = 200
        assertEquals(0, BigDecimal.valueOf(200).compareTo(resultado.totalCalorias()));
    }

    @Test
    void deveLancarExcecaoQuandoAlimentoNaoExiste() {
        when(alimentoRepository.findById(999)).thenReturn(Optional.empty());

        CalculoRequestDTO request = new CalculoRequestDTO(
                List.of(new ItemCalculoDTO(999, BigDecimal.valueOf(100)))
        );

        assertThrows(RuntimeException.class, () -> service.calcular(request));
    }

    @Test
    void naoDeveQuebrarQuandoCaloriasSaoNulas() {
        Alimento alimentoSemCalorias = new Alimento();
        alimentoSemCalorias.setIdAlimento(3);
        alimentoSemCalorias.setCaloriasPor100g(null);

        when(alimentoRepository.findById(3)).thenReturn(Optional.of(alimentoSemCalorias));
        when(alimentoMacroRepository.findByAlimento_IdAlimento(3)).thenReturn(List.of());
        when(alimentoMicroRepository.findByAlimento_IdAlimento(3)).thenReturn(List.of());

        CalculoRequestDTO request = new CalculoRequestDTO(
                List.of(new ItemCalculoDTO(3, BigDecimal.valueOf(100)))
        );

        CalculoResponseDTO resultado = service.calcular(request);

        assertEquals(0, BigDecimal.ZERO.compareTo(resultado.totalCalorias()));
    }
}