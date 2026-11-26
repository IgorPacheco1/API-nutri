package br.com.igorpacheco.apinutri.importer;

import br.com.igorpacheco.apinutri.model.Alimento;
import br.com.igorpacheco.apinutri.model.Categoria;
import br.com.igorpacheco.apinutri.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TacoImportServiceTest {

    @Mock
    private AlimentoRepository alimentoRepository;
    @Mock
    private AlimentoMacroRepository alimentoMacroRepository;
    @Mock
    private AlimentoMicroRepository alimentoMicroRepository;
    @Mock
    private MacroNutrienteRepository macroNutrienteRepository;
    @Mock
    private MicroNutrienteRepository microNutrienteRepository;
    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private TacoImportService service;

    /** Banco publicado na v1.0.0: alimentos já existem, mas sem categoria. */
    @Test
    void comBancoJaPopuladoDeveCompletarAsCategoriasSemImportarDeNovo() {
        Alimento arroz = alimento(1, "Arroz, integral, cozido");
        Alimento feijao = alimento(561, "Feijão, carioca, cozido");
        Alimento desconhecido = alimento(900, "Alimento que não está na TACO");

        when(alimentoRepository.count()).thenReturn(597L);
        when(alimentoRepository.findByCategoriaIsNull()).thenReturn(List.of(arroz, feijao, desconhecido));
        when(categoriaRepository.findByNomeCategoria(any())).thenReturn(Optional.empty());
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        service.executar();

        assertEquals("Cereais e derivados", arroz.getCategoria().getNomeCategoria());
        assertEquals("Leguminosas e derivados", feijao.getCategoria().getNomeCategoria());
        assertNull(desconhecido.getCategoria());
        verify(alimentoRepository).saveAll(List.of(arroz, feijao, desconhecido));
        verify(alimentoRepository, never()).save(any());
        verifyNoInteractions(alimentoMacroRepository, alimentoMicroRepository);
    }

    @Test
    void deveReaproveitarCategoriaQueJaExisteNoBanco() {
        Categoria existente = new Categoria("Cereais e derivados");
        existente.setIdCategoria(7);
        Alimento arroz = alimento(1, "Arroz, integral, cozido");
        Alimento arrozCru = alimento(2, "Arroz, integral, cru");

        when(alimentoRepository.count()).thenReturn(597L);
        when(alimentoRepository.findByCategoriaIsNull()).thenReturn(List.of(arroz, arrozCru));
        when(categoriaRepository.findByNomeCategoria("Cereais e derivados")).thenReturn(Optional.of(existente));

        service.executar();

        assertSame(existente, arroz.getCategoria());
        assertSame(existente, arrozCru.getCategoria());
        verify(categoriaRepository, times(1)).findByNomeCategoria("Cereais e derivados");
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void comTodasAsCategoriasPreenchidasNaoDeveAlterarNada() {
        when(alimentoRepository.count()).thenReturn(597L);
        when(alimentoRepository.findByCategoriaIsNull()).thenReturn(List.of());

        service.executar();

        verify(alimentoRepository, never()).saveAll(any());
        verifyNoInteractions(categoriaRepository);
    }

    @Test
    void deveConverterOsValoresEspeciaisDaTaco() {
        assertNull(TacoImportService.converterValorNutrientes("NA"));
        assertEquals(0.0, TacoImportService.converterValorNutrientes("Tr"));
        assertEquals(25.8, TacoImportService.converterValorNutrientes("25.8"));
        assertEquals(1e-05, TacoImportService.converterValorNutrientes("1e-05"));
    }

    private static Alimento alimento(int id, String nome) {
        Alimento alimento = new Alimento();
        alimento.setIdAlimento(id);
        alimento.setNomeAlimento(nome);
        return alimento;
    }
}
