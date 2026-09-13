package br.com.igorpacheco.apinutri.importer;

import br.com.igorpacheco.apinutri.model.Alimento;
import br.com.igorpacheco.apinutri.model.AlimentoMacro;
import br.com.igorpacheco.apinutri.repository.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStreamReader;
import java.io.Reader;

public class TacoCsvImporter implements CommandLineRunner {
    private final AlimentoRepository alimentoRepository;
    private final AlimentoMacroRepository macroRepository;
    private final AlimentoMicroRepository microRepository;
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
                double kcalAlimento = Double.parseDouble(record.get("Energia..kcal."));
                double proteinaAlimento = converterValorNutrientes(record.get("Proteína..g."));

                Alimento alimento = new Alimento();
                alimento.setNomeAlimento(nomeAlimento);
                alimento.setCaloriasPor100g(kcalAlimento);
                if(proteinaAlimento != null){

                }
                Alimento alimentosalv0 = alimentoRepository.save(alimento);


            }
        }
    }
}
