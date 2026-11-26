package br.com.igorpacheco.apinutri.controller;

import br.com.igorpacheco.apinutri.dto.AlimentoDTO;
import br.com.igorpacheco.apinutri.dto.AlimentoDetalheDTO;
import br.com.igorpacheco.apinutri.service.AlimentoService;
import br.com.igorpacheco.apinutri.service.CatalogoExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/alimentos")
@RequiredArgsConstructor

@Tag(name = "Alimentos", description = "Busca, detalhe e exportação do catálogo de alimentos da TACO")
public class AlimentoController {

    static final MediaType TEXT_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);
    static final MediaType APPLICATION_SQL = new MediaType("application", "sql", StandardCharsets.UTF_8);

    /** O catálogo só muda quando o importador roda; navegadores podem guardar as respostas. */
    private static final CacheControl CACHE_BUSCA = CacheControl.maxAge(Duration.ofHours(1)).cachePublic();
    private static final CacheControl CACHE_CATALOGO = CacheControl.maxAge(Duration.ofDays(1)).cachePublic();

    private final AlimentoService alimentoService;
    private final CatalogoExportService catalogoExportService;

    @GetMapping
    @Operation(summary = "Busca alimentos pelo nome",
            description = "Ignora acentos, maiúsculas e pontuação (\"feijao\" encontra \"Feijão, carioca, cozido\"). "
                    + "Cada palavra digitada precisa ser o começo de uma palavra do nome. "
                    + "Cada resultado traz a categoria e os valores por 100 g.")
    public ResponseEntity<List<AlimentoDTO>> buscar(
            @Parameter(description = "Texto da busca (1 a 100 caracteres)", example = "feijao")
            @RequestParam String nome,
            @Parameter(description = "Quantidade máxima de resultados (1 a 50, padrão 20)", example = "20")
            @RequestParam(required = false) Integer limite) {
        return ResponseEntity.ok()
                .cacheControl(CACHE_BUSCA)
                .body(alimentoService.buscarPorNome(nome, limite));
    }

    @GetMapping("/{idAlimento:\\d+}")
    @Operation(summary = "Detalhe de um alimento",
            description = "Mesmos campos da busca, mais a lista de micronutrientes com a unidade de cada um.")
    public ResponseEntity<AlimentoDetalheDTO> detalhar(@PathVariable int idAlimento) {
        return ResponseEntity.ok()
                .cacheControl(CACHE_CATALOGO)
                .body(alimentoService.buscarPorId(idAlimento));
    }

    @GetMapping("/catalogo")
    @Operation(summary = "Catálogo completo em JSON",
            description = "Todos os alimentos, ordenados pelo ID, com categoria e valores por 100 g.")
    public ResponseEntity<List<AlimentoDTO>> catalogo() {
        return ResponseEntity.ok()
                .cacheControl(CACHE_CATALOGO)
                .body(alimentoService.listarCatalogo());
    }

    @GetMapping(value = "/catalogo/csv", produces = "text/csv")
    @Operation(summary = "Catálogo completo em CSV",
            description = "Colunas: id_api_nutri, categoria, nome, porcao_desc, porcao_g, kcal_100g, carb_100g, "
                    + "prot_100g, gord_100g, sodio_100g_mg. Separador vírgula, decimal com ponto, UTF-8.")
    public ResponseEntity<byte[]> catalogoCsv() {
        String csv = catalogoExportService.gerarCsv(alimentoService.listarCatalogo());
        return arquivo(csv, TEXT_CSV, "catalogo-taco.csv");
    }

    @GetMapping(value = "/catalogo/mysql", produces = "application/sql")
    @Operation(summary = "Script MySQL para carregar o catálogo no PratoFácil",
            description = "Gera INSERTs nas tabelas categoria e alimento do banco do PratoFácil (MySQL 8). "
                    + "Pode ser rodado mais de uma vez sem duplicar registros.")
    public ResponseEntity<byte[]> catalogoMySql() {
        String sql = catalogoExportService.gerarScriptMySql(alimentoService.listarCatalogo());
        return arquivo(sql, APPLICATION_SQL, "carga-catalogo-taco-pratofacil.sql");
    }

    private static ResponseEntity<byte[]> arquivo(String conteudo, MediaType tipo, String nomeArquivo) {
        return ResponseEntity.ok()
                .contentType(tipo)
                .cacheControl(CACHE_CATALOGO)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nomeArquivo).build().toString())
                .body(conteudo.getBytes(StandardCharsets.UTF_8));
    }
}
