package br.com.igorpacheco.apinutri.service;

import br.com.igorpacheco.apinutri.dto.AlimentoDTO;
import br.com.igorpacheco.apinutri.dto.AlimentoDetalheDTO;
import br.com.igorpacheco.apinutri.dto.MicroNutrienteDTO;
import br.com.igorpacheco.apinutri.dto.NutrientesPor100gDTO;
import br.com.igorpacheco.apinutri.exception.RecusoNaoEncontradoException;
import br.com.igorpacheco.apinutri.exception.RequisicaoInvalidaException;
import br.com.igorpacheco.apinutri.model.Alimento;
import br.com.igorpacheco.apinutri.model.AlimentoMicro;
import br.com.igorpacheco.apinutri.model.Nutrientes;
import br.com.igorpacheco.apinutri.repository.AlimentoMacroRepository;
import br.com.igorpacheco.apinutri.repository.AlimentoMicroRepository;
import br.com.igorpacheco.apinutri.repository.AlimentoRepository;
import br.com.igorpacheco.apinutri.repository.ValorNutriente;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Busca e catálogo de alimentos.
 * <p>
 * O catálogo da TACO é pequeno (597 alimentos) e só muda quando o importador roda,
 * então ele é carregado do banco uma vez (3 consultas) e guardado em memória.
 * A busca roda em memória: é rápida e ignora acentos sem depender de extensão do banco.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlimentoService {

    public static final int LIMITE_PADRAO = 20;
    public static final int LIMITE_MAXIMO = 50;
    public static final int TAMANHO_MAXIMO_BUSCA = 100;

    private final AlimentoRepository alimentoRepository;
    private final AlimentoMacroRepository alimentoMacroRepository;
    private final AlimentoMicroRepository alimentoMicroRepository;

    private volatile Catalogo cache;

    private record ItemCatalogo(AlimentoDTO alimento, String nomeNormalizado, List<String> palavras) {
    }

    private record Catalogo(List<ItemCatalogo> itens, Map<Integer, AlimentoDTO> porId) {
    }

    public List<AlimentoDTO> buscarPorNome(String nome, Integer limite) {
        if (nome == null || nome.isBlank()) {
            throw new RequisicaoInvalidaException("O parâmetro 'nome' é obrigatório e não pode ficar em branco.");
        }
        if (nome.length() > TAMANHO_MAXIMO_BUSCA) {
            throw new RequisicaoInvalidaException(
                    "O parâmetro 'nome' deve ter no máximo " + TAMANHO_MAXIMO_BUSCA + " caracteres.");
        }
        int limiteEfetivo = (limite == null) ? LIMITE_PADRAO : limite;
        if (limiteEfetivo < 1 || limiteEfetivo > LIMITE_MAXIMO) {
            throw new RequisicaoInvalidaException(
                    "O parâmetro 'limite' deve estar entre 1 e " + LIMITE_MAXIMO + ".");
        }

        List<String> termos = TextoBusca.palavras(nome);
        if (termos.isEmpty()) {
            return List.of();
        }
        String termoCompleto = String.join(" ", termos);
        String primeiroTermo = termos.get(0);

        return catalogo().itens().stream()
                .filter(item -> TextoBusca.contemTodos(item.palavras(), termos))
                .sorted(Comparator
                        .comparingInt((ItemCatalogo item) -> relevancia(item, termoCompleto, primeiroTermo))
                        .thenComparingInt(item -> item.palavras().size())
                        .thenComparing(ItemCatalogo::nomeNormalizado))
                .limit(limiteEfetivo)
                .map(ItemCatalogo::alimento)
                .toList();
    }

    /** 0 = o nome começa com o que foi digitado; 1 = a primeira palavra bate; 2 = aparece no meio. */
    private int relevancia(ItemCatalogo item, String termoCompleto, String primeiroTermo) {
        if (item.nomeNormalizado().startsWith(termoCompleto)) {
            return 0;
        }
        if (!item.palavras().isEmpty() && item.palavras().get(0).startsWith(primeiroTermo)) {
            return 1;
        }
        return 2;
    }

    public AlimentoDetalheDTO buscarPorId(int idAlimento) {
        AlimentoDTO alimento = catalogo().porId().get(idAlimento);
        if (alimento == null) {
            throw new RecusoNaoEncontradoException("Alimento não encontrado: " + idAlimento);
        }
        List<MicroNutrienteDTO> micros = alimentoMicroRepository.findByAlimento_IdAlimento(idAlimento).stream()
                .filter(m -> m.getValorPor100g() != null)
                .map(this::paraMicroDTO)
                .sorted(Comparator.comparing(MicroNutrienteDTO::nome))
                .toList();
        return new AlimentoDetalheDTO(alimento.idAlimento(), alimento.nomeAlimento(), alimento.categoria(),
                alimento.por100g(), micros);
    }

    /** Catálogo completo, ordenado pelo ID. Usado pelas rotas de exportação. */
    public List<AlimentoDTO> listarCatalogo() {
        return catalogo().itens().stream().map(ItemCatalogo::alimento).toList();
    }

    /** Descarta o catálogo em memória; a próxima chamada lê o banco de novo. */
    public void invalidarCache() {
        cache = null;
    }

    private Catalogo catalogo() {
        Catalogo atual = cache;
        if (atual == null) {
            synchronized (this) {
                atual = cache;
                if (atual == null) {
                    atual = carregarCatalogo();
                    // catálogo vazio não fica em cache: o importador pode ainda estar rodando
                    if (!atual.itens().isEmpty()) {
                        cache = atual;
                    }
                }
            }
        }
        return atual;
    }

    private Catalogo carregarCatalogo() {
        List<Alimento> alimentos = alimentoRepository.findAllByOrderByIdAlimentoAsc();

        Map<Integer, Map<String, BigDecimal>> macrosPorAlimento = new HashMap<>();
        for (ValorNutriente valor : alimentoMacroRepository.listarValoresDeMacros()) {
            if (valor.valorPor100g() != null) {
                macrosPorAlimento
                        .computeIfAbsent(valor.idAlimento(), id -> new HashMap<>())
                        .put(Nutrientes.normalizar(valor.nomeNutriente()), valor.valorPor100g());
            }
        }

        Map<Integer, BigDecimal> sodioPorAlimento = new HashMap<>();
        for (ValorNutriente valor : alimentoMicroRepository.listarValoresDoMicronutriente(Nutrientes.SODIO)) {
            if (valor.valorPor100g() != null) {
                sodioPorAlimento.put(valor.idAlimento(), valor.valorPor100g());
            }
        }

        List<ItemCatalogo> itens = new ArrayList<>(alimentos.size());
        Map<Integer, AlimentoDTO> porId = new HashMap<>();
        for (Alimento alimento : alimentos) {
            Map<String, BigDecimal> macros = macrosPorAlimento.getOrDefault(alimento.getIdAlimento(), Map.of());
            NutrientesPor100gDTO por100g = new NutrientesPor100gDTO(
                    umaCasa(alimento.getCaloriasPor100g()),
                    umaCasa(macros.get(Nutrientes.CARBOIDRATO)),
                    umaCasa(macros.get(Nutrientes.PROTEINA)),
                    umaCasa(macros.get(Nutrientes.LIPIDEOS)),
                    umaCasa(sodioPorAlimento.get(alimento.getIdAlimento())));
            String categoria = alimento.getCategoria() == null ? null : alimento.getCategoria().getNomeCategoria();
            AlimentoDTO dto = new AlimentoDTO(alimento.getIdAlimento(), alimento.getNomeAlimento(), categoria, por100g);

            String nomeNormalizado = TextoBusca.normalizar(alimento.getNomeAlimento());
            itens.add(new ItemCatalogo(dto, nomeNormalizado, TextoBusca.palavras(alimento.getNomeAlimento())));
            porId.put(dto.idAlimento(), dto);
        }
        log.info("Catálogo de alimentos carregado em memória: {} alimentos", itens.size());
        return new Catalogo(List.copyOf(itens), Map.copyOf(porId));
    }

    private MicroNutrienteDTO paraMicroDTO(AlimentoMicro micro) {
        return new MicroNutrienteDTO(
                micro.getMicroNutriente().getNomeMicros(),
                micro.getValorPor100g().setScale(2, RoundingMode.HALF_UP),
                micro.getMicroNutriente().getUnidade());
    }

    private static BigDecimal umaCasa(BigDecimal valor) {
        return valor == null ? null : valor.setScale(1, RoundingMode.HALF_UP);
    }
}
