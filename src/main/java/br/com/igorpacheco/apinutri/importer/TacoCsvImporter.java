package br.com.igorpacheco.apinutri.importer;

import br.com.igorpacheco.apinutri.model.*;
import br.com.igorpacheco.apinutri.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

@Component
@RequiredArgsConstructor

public class TacoCsvImporter implements CommandLineRunner {
    private final AlimentoRepository alimentoRepository;
    private final AlimentoMacroRepository alimentoMacroRepository;
    private final AlimentoMicroRepository alimentoMicroRepository;
    private final MacroNutrienteRepository macroNutrienteRepository;
    private final MicroNutrienteRepository microNutrienteRepository;


    private Double converterValorNutrientes(String valorTexto) {
        if (valorTexto.equals("NA")) {
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
        MacroNutriente macro = macroNutrienteRepository.findByNomeMacros(nomeMacro).orElseThrow(() -> new RuntimeException("Macronutriente " + nomeMacro + " não cadastrado"));

        AlimentoMacroId id = new AlimentoMacroId(alimentoSalvo.getIdAlimento(), macro.getIdMacros());

        AlimentoMacro alimentoMacro = new AlimentoMacro();
        alimentoMacro.setId(id);
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

        MicroNutriente micro = microNutrienteRepository.findByNomeMicros(nomeMicro).orElseThrow(() -> new RuntimeException("Micronutriente " + nomeMicro + " não cadastrado"));
        AlimentoMicroId id = new AlimentoMicroId(alimentoSalvo.getIdAlimento(), micro.getId());
        AlimentoMicro alimentoMicro = new AlimentoMicro();
        alimentoMicro.setId(id);
        alimentoMicro.setAlimento(alimentoSalvo);
        alimentoMicro.setMicroNutriente(micro);
        alimentoMicro.setGramasPor100g(BigDecimal.valueOf(valor));
        alimentoMicroRepository.save(alimentoMicro);

    }

    @Override
    public void run(String... strings) throws Exception {
        if (alimentoRepository.count() > 0) {
            return;
        }
        String conteudoArquivo = new String(
                new ClassPathResource("data/alimentos.csv").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );
        conteudoArquivo = Normalizer.normalize(conteudoArquivo, Normalizer.Form.NFC);
        conteudoArquivo = conteudoArquivo.replace('\u00A0', ' ');

        try (StringReader reader = new StringReader(conteudoArquivo)) {

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build();

            CSVParser parser = format.parse(reader);
            for (CSVRecord record : parser) {
                String nomeAlimento = record.get("Descrição dos alimentos");
                Double kcalValor = converterValorNutrientes(record.get("Energia..kcal."));
                BigDecimal kcalAlimento = (kcalValor != null) ? BigDecimal.valueOf(kcalValor) : null;
                Double proteinaAlimento = converterValorNutrientes(record.get("Proteína..g."));

                Alimento alimento = new Alimento();
                alimento.setNomeAlimento(nomeAlimento);
                alimento.setCaloriasPor100g(kcalAlimento);
                Alimento alimentosalvo = alimentoRepository.save(alimento);

                processarMacro(record, "Proteína..g.", "Proteína", alimentosalvo);
                processarMacro(record, "Lipídeos..g.", "Lipídios", alimentosalvo);
                processarMacro(record, "Carboidrato..g.", "Carboidrato", alimentosalvo);

                processarMicro(record, "Colesterol..mg.", "Colesterol", alimentosalvo);
                processarMicro(record, "Cálcio..mg.", "Cálcio", alimentosalvo);
                processarMicro(record, "Magnésio..mg.", "Magnésio", alimentosalvo);
                processarMicro(record, "Manganês..mg.", "Manganês", alimentosalvo);
                processarMicro(record, "Fósforo..mg.", "Fósforo", alimentosalvo);
                processarMicro(record, "Ferro..mg.", "Ferro", alimentosalvo);
                processarMicro(record, "Sódio..mg.", "Sódio", alimentosalvo);
                processarMicro(record, "Potássio..mg.", "Potássio", alimentosalvo);
                processarMicro(record, "Cobre..mg.", "Cobre", alimentosalvo);
                processarMicro(record, "Zinco..mg.", "Zinco", alimentosalvo);
                processarMicro(record, "RAE..mcg.", "Vitamina A", alimentosalvo);
                processarMicro(record, "Tiamina..mg.", "Tiamina", alimentosalvo);
                processarMicro(record, "Riboflavina..mg.", "Riboflavina", alimentosalvo);
                processarMicro(record, "Piridoxina..mg.", "Piridoxina", alimentosalvo);
                processarMicro(record, "Niacina..mg.", "Niacina", alimentosalvo);
                processarMicro(record, "Vitamina.C..mg.", "Vitamina C", alimentosalvo);

            }


        }
    }
}
