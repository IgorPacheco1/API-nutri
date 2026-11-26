package br.com.igorpacheco.apinutri.service;

import br.com.igorpacheco.apinutri.dto.AlimentoDTO;
import br.com.igorpacheco.apinutri.dto.AlimentoDetalheDTO;
import br.com.igorpacheco.apinutri.exception.RecusoNaoEncontradoException;
import br.com.igorpacheco.apinutri.exception.RequisicaoInvalidaException;
import br.com.igorpacheco.apinutri.model.*;
import br.com.igorpacheco.apinutri.repository.AlimentoMacroRepository;
import br.com.igorpacheco.apinutri.repository.AlimentoMicroRepository;
import br.com.igorpacheco.apinutri.repository.AlimentoRepository;
import br.com.igorpacheco.apinutri.repository.ValorNutriente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AlimentoServiceTest {

    @Mock
    private AlimentoRepository alimentoRepository;

    @Mock
    private AlimentoMacroRepository alimentoMacroRepository;

    @Mock
    private AlimentoMicroRepository alimentoMicroRepository;

    @InjectMocks
    private AlimentoService service;

    private void catalogo(List<Alimento> alimentos, List<ValorNutriente> macros, List<ValorNutriente> sodio) {
        when(alimentoRepository.findAllByOrderByIdAlimentoAsc()).thenReturn(alimentos);
        when(alimentoMacroRepository.listarValoresDeMacros()).thenReturn(macros);
        when(alimentoMicroRepository.listarValoresDoMicronutriente(anyString())).thenReturn(sodio);
    }

    private void catalogo(Alimento... alimentos) {
        catalogo(List.of(alimentos), List.of(), List.of());
    }

    @Test
    void buscaDeveIgnorarAcentosEMaiusculas() {
        catalogo(alimento(1, "Feijão, carioca, cozido", "Leguminosas e derivados"),
                alimento(2, "Arroz, integral, cozido", "Cereais e derivados"));

        List<AlimentoDTO> resultado = service.buscarPorNome("FEIJAO", null);

        assertEquals(1, resultado.size());
        assertEquals("Feijão, carioca, cozido", resultado.get(0).nomeAlimento());
    }

    @Test
    void buscaComVariasPalavrasExigeTodasElasNoNome() {
        catalogo(alimento(1, "Arroz, integral, cozido", "Cereais e derivados"),
                alimento(2, "Arroz, tipo 1, cozido", "Cereais e derivados"),
                alimento(3, "Arroz, integral, cru", "Cereais e derivados"));

        List<String> nomes = service.buscarPorNome("arroz integral", null).stream()
                .map(AlimentoDTO::nomeAlimento).toList();

        assertEquals(List.of("Arroz, integral, cozido", "Arroz, integral, cru"), nomes);
    }

    @Test
    void buscaDeveMostrarPrimeiroOsNomesQueComecamComOTermo() {
        catalogo(alimento(1, "Baião de dois, arroz e feijão-de-corda", "Alimentos preparados"),
                alimento(2, "Farinha, de arroz, enriquecida", "Cereais e derivados"),
                alimento(3, "Arroz, tipo 1, cozido", "Cereais e derivados"));

        List<String> nomes = service.buscarPorNome("arroz", null).stream()
                .map(AlimentoDTO::nomeAlimento).toList();

        assertEquals("Arroz, tipo 1, cozido", nomes.get(0));
        assertEquals(3, nomes.size());
    }

    @Test
    void buscaDeveLimitarA20ResultadosPorPadraoEAceitarOutroLimite() {
        List<Alimento> alimentos = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            alimentos.add(alimento(i, "Queijo, tipo " + i, "Leite e derivados"));
        }
        catalogo(alimentos, List.of(), List.of());

        assertEquals(AlimentoService.LIMITE_PADRAO, service.buscarPorNome("queijo", null).size());
        assertEquals(5, service.buscarPorNome("queijo", 5).size());
        assertEquals(30, service.buscarPorNome("queijo", AlimentoService.LIMITE_MAXIMO).size());
    }

    @Test
    void buscaDeveRecusarLimiteForaDaFaixa() {
        RequisicaoInvalidaException zero = assertThrows(RequisicaoInvalidaException.class,
                () -> service.buscarPorNome("arroz", 0));
        assertTrue(zero.getMessage().contains("limite"));
        assertThrows(RequisicaoInvalidaException.class, () -> service.buscarPorNome("arroz", 51));
    }

    @Test
    void buscaDeveRecusarNomeEmBrancoOuMuitoLongo() {
        assertThrows(RequisicaoInvalidaException.class, () -> service.buscarPorNome("   ", null));
        assertThrows(RequisicaoInvalidaException.class, () -> service.buscarPorNome("a".repeat(101), null));
    }

    @Test
    void buscaSoComPontuacaoDevolveListaVazia() {
        catalogo(alimento(1, "Arroz, tipo 1, cozido", "Cereais e derivados"));

        assertEquals(List.of(), service.buscarPorNome("!!!", null));
    }

    @Test
    void resultadoDeveTrazerCategoriaEValoresPor100g() {
        Alimento arroz = alimento(1, "Arroz, integral, cozido", "Cereais e derivados");
        arroz.setCaloriasPor100g(BigDecimal.valueOf(124));
        Alimento sal = alimento(2, "Sal, grosso", "Miscelâneas");
        catalogo(List.of(arroz, sal),
                List.of(new ValorNutriente(1, "Carboidrato", BigDecimal.valueOf(25.8)),
                        new ValorNutriente(1, "Proteína", BigDecimal.valueOf(2.6)),
                        new ValorNutriente(1, "Lipídeos", BigDecimal.valueOf(1.0))),
                List.of(new ValorNutriente(1, "Sódio", new BigDecimal("1E-5")),
                        new ValorNutriente(2, "Sódio", BigDecimal.valueOf(39943))));

        AlimentoDTO dtoArroz = service.buscarPorNome("arroz", null).get(0);
        assertEquals("Cereais e derivados", dtoArroz.categoria());
        assertEquals(new BigDecimal("124.0"), dtoArroz.por100g().kcal());
        assertEquals(new BigDecimal("25.8"), dtoArroz.por100g().carboidratoG());
        assertEquals(new BigDecimal("2.6"), dtoArroz.por100g().proteinaG());
        assertEquals(new BigDecimal("1.0"), dtoArroz.por100g().gorduraG());
        assertEquals(new BigDecimal("0.0"), dtoArroz.por100g().sodioMg(), "traço (1e-05) vira 0.0");

        AlimentoDTO dtoSal = service.buscarPorNome("sal", null).get(0);
        assertNull(dtoSal.por100g().kcal(), "nutriente não analisado (NA) fica null");
        assertNull(dtoSal.por100g().gorduraG());
        assertEquals(new BigDecimal("39943.0"), dtoSal.por100g().sodioMg());
    }

    @Test
    void catalogoDeveSerLidoDoBancoUmaVezEAteSerInvalidado() {
        catalogo(alimento(1, "Arroz, tipo 1, cozido", "Cereais e derivados"));

        service.buscarPorNome("arroz", null);
        service.listarCatalogo();
        verify(alimentoRepository, times(1)).findAllByOrderByIdAlimentoAsc();

        service.invalidarCache();
        service.listarCatalogo();
        verify(alimentoRepository, times(2)).findAllByOrderByIdAlimentoAsc();
    }

    @Test
    void catalogoVazioNaoDeveFicarEmCache() {
        catalogo();

        assertEquals(List.of(), service.listarCatalogo());
        service.listarCatalogo();

        verify(alimentoRepository, times(2)).findAllByOrderByIdAlimentoAsc();
    }

    @Test
    void detalheDeveTrazerMicronutrientesComUnidade() {
        catalogo(alimento(1, "Arroz, integral, cozido", "Cereais e derivados"));
        when(alimentoMicroRepository.findByAlimento_IdAlimento(1))
                .thenReturn(List.of(micro("Sódio", "mg", 1), micro("Cálcio", "mg", 5), micro("Vitamina A", "mcg", 0.5)));

        AlimentoDetalheDTO detalhe = service.buscarPorId(1);

        assertEquals("Cereais e derivados", detalhe.categoria());
        assertEquals(3, detalhe.micros().size());
        assertEquals("Cálcio", detalhe.micros().get(0).nome());
        assertEquals("mcg", detalhe.micros().get(2).unidade());
        assertEquals(new BigDecimal("0.50"), detalhe.micros().get(2).valorPor100g());
    }

    @Test
    void detalheDeAlimentoInexistenteDeveLancarNaoEncontrado() {
        catalogo(alimento(1, "Arroz, integral, cozido", "Cereais e derivados"));

        assertThrows(RecusoNaoEncontradoException.class, () -> service.buscarPorId(999));
    }

    private static Alimento alimento(int id, String nome, String categoria) {
        Alimento alimento = new Alimento();
        alimento.setIdAlimento(id);
        alimento.setNomeAlimento(nome);
        alimento.setCategoria(new Categoria(categoria));
        return alimento;
    }

    private static AlimentoMicro micro(String nome, String unidade, double valor) {
        MicroNutriente microNutriente = new MicroNutriente();
        microNutriente.setNomeMicros(nome);
        microNutriente.setUnidade(unidade);
        AlimentoMicro alimentoMicro = new AlimentoMicro();
        alimentoMicro.setMicroNutriente(microNutriente);
        alimentoMicro.setValorPor100g(BigDecimal.valueOf(valor));
        return alimentoMicro;
    }
}
