package br.com.igorpacheco.apinutri.importer;

import br.com.igorpacheco.apinutri.model.*;
import br.com.igorpacheco.apinutri.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;

/**
 * Carrega a Tabela TACO (src/main/resources/data/alimentos.csv) no banco.
 * <ul>
 *   <li>Tabela "alimentos" vazia: importa tudo (alimentos, categorias, macros e micros).</li>
 *   <li>Tabela já populada (ex.: banco publicado na v1.0.0): só completa a categoria dos
 *       alimentos que estão sem categoria, casando pelo nome do alimento.</li>
 * </ul>
 * Tudo roda em uma transação: se algo falhar no meio, nada fica pela metade.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TacoImportService {

    static final String ARQUIVO_CSV = "data/alimentos.csv";
    static final String COLUNA_NOME = "Descrição dos alimentos";
    static final String COLUNA_CATEGORIA = "Categoria do alimento";

    private final AlimentoRepository alimentoRepository;
    private final AlimentoMacroRepository alimentoMacroRepository;
    private final AlimentoMicroRepository alimentoMicroRepository;
    private final MacroNutrienteRepository macroNutrienteRepository;
    private final MicroNutrienteRepository microNutrienteRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional
    public void executar() {
        List<CSVRecord> registros = lerCsv();
        if (alimentoRepository.count() == 0) {
            importarTudo(registros);
        } else {
            completarCategorias(registros);
        }
    }

    private void importarTudo(List<CSVRecord> registros) {
        Map<String, Categoria> categorias = new HashMap<>();
        for (CSVRecord record : registros) {
            Alimento alimento = new Alimento();
            alimento.setNomeAlimento(record.get(COLUNA_NOME));
            Double kcal = converterValorNutrientes(record.get("Energia..kcal."));
            alimento.setCaloriasPor100g(kcal != null ? BigDecimal.valueOf(kcal) : null);
            alimento.setCategoria(obterOuCriarCategoria(record.get(COLUNA_CATEGORIA), categorias));
            Alimento alimentoSalvo = alimentoRepository.save(alimento);

            processarMacro(record, "Proteína..g.", Nutrientes.PROTEINA, alimentoSalvo);
            processarMacro(record, "Lipídeos..g.", Nutrientes.LIPIDEOS, alimentoSalvo);
            processarMacro(record, "Carboidrato..g.", Nutrientes.CARBOIDRATO, alimentoSalvo);

            processarMicro(record, "Colesterol..mg.", "Colesterol", alimentoSalvo);
            processarMicro(record, "Cálcio..mg.", "Cálcio", alimentoSalvo);
            processarMicro(record, "Magnésio..mg.", "Magnésio", alimentoSalvo);
            processarMicro(record, "Manganês..mg.", "Manganês", alimentoSalvo);
            processarMicro(record, "Fósforo..mg.", "Fósforo", alimentoSalvo);
            processarMicro(record, "Ferro..mg.", "Ferro", alimentoSalvo);
            processarMicro(record, "Sódio..mg.", Nutrientes.SODIO, alimentoSalvo);
            processarMicro(record, "Potássio..mg.", "Potássio", alimentoSalvo);
            processarMicro(record, "Cobre..mg.", "Cobre", alimentoSalvo);
            processarMicro(record, "Zinco..mg.", "Zinco", alimentoSalvo);
            processarMicro(record, "RAE..mcg.", "Vitamina A", alimentoSalvo);
            processarMicro(record, "Tiamina..mg.", "Tiamina", alimentoSalvo);
            processarMicro(record, "Riboflavina..mg.", "Riboflavina", alimentoSalvo);
            processarMicro(record, "Piridoxina..mg.", "Piridoxina", alimentoSalvo);
            processarMicro(record, "Niacina..mg.", "Niacina", alimentoSalvo);
            processarMicro(record, "Vitamina.C..mg.", "Vitamina C", alimentoSalvo);
        }
        log.info("Importação da TACO concluída: {} alimentos e {} categorias", registros.size(), categorias.size());
    }

    private void completarCategorias(List<CSVRecord> registros) {
        List<Alimento> semCategoria = alimentoRepository.findByCategoriaIsNull();
        if (semCategoria.isEmpty()) {
            return;
        }
        Map<String, String> categoriaPorNome = new HashMap<>();
        for (CSVRecord record : registros) {
            categoriaPorNome.put(chaveDeNome(record.get(COLUNA_NOME)), record.get(COLUNA_CATEGORIA));
        }

        Map<String, Categoria> categorias = new HashMap<>();
        int preenchidos = 0;
        for (Alimento alimento : semCategoria) {
            String nomeCategoria = categoriaPorNome.get(chaveDeNome(alimento.getNomeAlimento()));
            if (nomeCategoria == null) {
                log.warn("Categoria não encontrada no CSV para o alimento {} ({})",
                        alimento.getIdAlimento(), alimento.getNomeAlimento());
                continue;
            }
            alimento.setCategoria(obterOuCriarCategoria(nomeCategoria, categorias));
            preenchidos++;
        }
        alimentoRepository.saveAll(semCategoria);
        log.info("Categorias completadas em {} de {} alimentos sem categoria", preenchidos, semCategoria.size());
    }

    private Categoria obterOuCriarCategoria(String nome, Map<String, Categoria> jaCarregadas) {
        String nomeLimpo = Nutrientes.normalizar(nome);
        if (nomeLimpo.isEmpty()) {
            return null;
        }
        return jaCarregadas.computeIfAbsent(nomeLimpo, n -> categoriaRepository.findByNomeCategoria(n)
                .orElseGet(() -> categoriaRepository.save(new Categoria(n))));
    }

    private static String chaveDeNome(String nome) {
        return Nutrientes.normalizar(nome == null ? "" : nome.replace(' ', ' ')).toLowerCase(Locale.ROOT);
    }

    private List<CSVRecord> lerCsv() {
        try {
            String conteudo = new String(
                    new ClassPathResource(ARQUIVO_CSV).getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
            conteudo = Normalizer.normalize(conteudo, Normalizer.Form.NFC).replace(' ', ' ');

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build();
            try (CSVParser parser = format.parse(new StringReader(conteudo))) {
                return parser.getRecords();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler " + ARQUIVO_CSV, e);
        }
    }

    static Double converterValorNutrientes(String valorTexto) {
        if (valorTexto == null || valorTexto.isEmpty() || valorTexto.equals("NA")) {
            return null;
        }
        if (valorTexto.equals("Tr")) {
            return 0.0;
        }
        return Double.parseDouble(valorTexto);
    }

    private void processarMacro(CSVRecord record, String colunaCSV, String nomeMacro, Alimento alimentoSalvo) {
        Double valor = converterValorNutrientes(record.get(colunaCSV));
        if (valor == null) {
            return;
        }
        MacroNutriente macro = macroNutrienteRepository.findByNomeMacros(nomeMacro)
                .orElseThrow(() -> new IllegalStateException("Macronutriente " + nomeMacro + " não cadastrado"));

        AlimentoMacro alimentoMacro = new AlimentoMacro();
        alimentoMacro.setId(new AlimentoMacroId(alimentoSalvo.getIdAlimento(), macro.getIdMacros()));
        alimentoMacro.setAlimento(alimentoSalvo);
        alimentoMacro.setMacroNutriente(macro);
        alimentoMacro.setGramasPor100g(BigDecimal.valueOf(valor));
        alimentoMacroRepository.save(alimentoMacro);
    }

    private void processarMicro(CSVRecord record, String colunaCsv, String nomeMicro, Alimento alimentoSalvo) {
        Double valor = converterValorNutrientes(record.get(colunaCsv));
        if (valor == null) {
            return;
        }
        MicroNutriente micro = microNutrienteRepository.findByNomeMicros(nomeMicro)
                .orElseThrow(() -> new IllegalStateException("Micronutriente " + nomeMicro + " não cadastrado"));

        AlimentoMicro alimentoMicro = new AlimentoMicro();
        alimentoMicro.setId(new AlimentoMicroId(alimentoSalvo.getIdAlimento(), micro.getId()));
        alimentoMicro.setAlimento(alimentoSalvo);
        alimentoMicro.setMicroNutriente(micro);
        alimentoMicro.setValorPor100g(BigDecimal.valueOf(valor));
        alimentoMicroRepository.save(alimentoMicro);
    }
}
