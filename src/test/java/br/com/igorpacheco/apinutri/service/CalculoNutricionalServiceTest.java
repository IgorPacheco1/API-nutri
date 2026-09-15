package br.com.igorpacheco.apinutri.service;

import br.com.igorpacheco.apinutri.dto.CalculoRequestDTO;
import br.com.igorpacheco.apinutri.dto.CalculoResponseDTO;
import br.com.igorpacheco.apinutri.dto.ItemCalculoDTO;
import br.com.igorpacheco.apinutri.model.Alimento;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CalculoNutricionalServiceTest {
    @Mock
    private AlimentoRepository alimentoRepository;

    @Mock
    private AlimentoMacroRepository alimentoMacroRepository;

    @Mock
    private AlimentoMicroRepository alimentoMicroRepository;

    @InjectMocks
    private CalculoNutricionalService service;

    @Test
    void deveCalcularCaloriasAcordoComQuantidade() {
        //prepara
        var arroz = new Alimento();
        arroz.setIdAlimento(1);
        arroz.setCaloriasPor100g(BigDecimal.valueOf(124));

        when(alimentoRepository.findById(1)).thenReturn(Optional.of(arroz));
        when(alimentoMacroRepository.findByAlimento_IdAlimento(1)).thenReturn(List.of());
        when(alimentoMicroRepository.findByAlimento_IdAlimento(1)).thenReturn(List.of());

        CalculoRequestDTO request = new CalculoRequestDTO(
                List.of(new ItemCalculoDTO(1, BigDecimal.valueOf(150)))
        );

        //executa
        CalculoResponseDTO resultado = service.calcular(request);

        //verifica
        assertEquals(BigDecimal.valueOf(186.000), resultado.totalCalorias());


    }
}
