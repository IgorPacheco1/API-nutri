package br.com.igorpacheco.apinutri.importer;

import br.com.igorpacheco.apinutri.model.Alimento;
import br.com.igorpacheco.apinutri.model.AlimentoMacro;
import br.com.igorpacheco.apinutri.model.AlimentoMacroId;
import br.com.igorpacheco.apinutri.model.MacroNutriente;
import br.com.igorpacheco.apinutri.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor

public class TacoCsvImporter implements CommandLineRunner {
    private final AlimentoRepository alimentoRepository;
    private final AlimentoMacroRepository alimentoMacroRepository;
    private final AlimentoMicroRepository alimentoMicroRepository;
    private final MacroNutrienteRepository macroNutrienteRepository;
    private final MicroNutrienteRepository microNutrienteRepository;



    private Double converterValorNutrientes(String valorTexto){
        if (valorTexto.equals("NA")){
            return null;
        }
        if (valorTexto.equals("Tr")){
            return 0.0;
        }
        return Double.parseDouble(valorTexto);
    }
    @Override
    public void run(String... strings) throws Exception {
        if (alimentoRepository.count() > 0) {
            return;
        }
        try (Reader reader = new InputStreamReader(
                new ClassPathResource("data/taco.csv").getInputStream())) {

            CSVParser parser = CSVFormat.newFormat('\t')
                    .withFirstRecordAsHeader()
                    .parse(reader);

            for (CSVRecord record : parser) {
                String nomeAlimento = record.get("Descrição dos alimentos");
                Double kcalAlimento = Double.parseDouble(record.get("Energia..kcal."));
                Double proteinaAlimento = converterValorNutrientes(record.get("Proteína..g."));

                Alimento alimento = new Alimento();
                alimento.setNomeAlimento(nomeAlimento);
                alimento.setCaloriasPor100g(kcalAlimento);
                Alimento alimentosalvo = alimentoRepository.save(alimento);

                if (proteinaAlimento != null) {
                    MacroNutriente macroProteina = macroNutrienteRepository.findByNomeMacros("Proteína")
                            .orElseThrow(() -> new RuntimeException("Macronutriente 'Proteína' não cadastrado"));

                    AlimentoMacroId id = new AlimentoMacroId(
                            alimentosalvo.getIdAlimento(),
                            macroProteina.getIdMacros()
                    );

                    AlimentoMacro alimentoMacro = new AlimentoMacro();
                    alimentoMacro.setId(id);
                    alimentoMacro.setAlimento(alimentosalvo);
                    alimentoMacro.setMacroNutriente(macroProteina);
                    alimentoMacro.setGramasPor100g(BigDecimal.valueOf(proteinaAlimento));

                    alimentoMacroRepository.save(alimentoMacro);
                }



            }
        }
    }
}
