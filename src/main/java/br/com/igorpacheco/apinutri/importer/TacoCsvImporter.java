package br.com.igorpacheco.apinutri.importer;

import br.com.igorpacheco.apinutri.service.AlimentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Roda a importação da TACO ao subir a aplicação.
 * Pode ser desligado com apinutri.importador.habilitado=false (usado nos testes).
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "apinutri.importador", name = "habilitado", havingValue = "true", matchIfMissing = true)
public class TacoCsvImporter implements CommandLineRunner {

    private final TacoImportService tacoImportService;
    private final AlimentoService alimentoService;

    @Override
    public void run(String... args) {
        tacoImportService.executar();
        // depois do commit: o catálogo em memória é recarregado na próxima requisição
        alimentoService.invalidarCache();
    }
}
