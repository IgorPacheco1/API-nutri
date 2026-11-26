package br.com.igorpacheco.apinutri.controller;

import br.com.igorpacheco.apinutri.importer.TacoImportService;
import br.com.igorpacheco.apinutri.model.Alimento;
import br.com.igorpacheco.apinutri.model.MacroNutriente;
import br.com.igorpacheco.apinutri.model.MicroNutriente;
import br.com.igorpacheco.apinutri.repository.AlimentoRepository;
import br.com.igorpacheco.apinutri.repository.CategoriaRepository;
import br.com.igorpacheco.apinutri.repository.MacroNutrienteRepository;
import br.com.igorpacheco.apinutri.repository.MicroNutrienteRepository;
import br.com.igorpacheco.apinutri.service.AlimentoService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CorsFilter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Sobe a aplicação inteira com H2 em memória, importa o CSV real da TACO
 * e testa as rotas de ponta a ponta (controller → service → repository → banco).
 */
@SpringBootTest
class ApiIntegracaoTest {

    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private TacoImportService tacoImportService;
    @Autowired
    private AlimentoService alimentoService;
    @Autowired
    private AlimentoRepository alimentoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private MacroNutrienteRepository macroNutrienteRepository;
    @Autowired
    private MicroNutrienteRepository microNutrienteRepository;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        if (alimentoRepository.count() == 0) {
            cadastrarNutrientes();
            tacoImportService.executar();
        }
        alimentoService.invalidarCache();
        mvc = MockMvcBuilders.webAppContextSetup(contexto)
                .addFilters(contexto.getBean(CorsFilter.class))
                .build();
    }

    @AfterEach
    void limparCache() {
        alimentoService.invalidarCache();
    }

    // ---------- importação ----------

    @Test
    void importacaoDeveGravarTodosOsAlimentosComCategoria() {
        assertEquals(597, alimentoRepository.count());
        assertEquals(15, categoriaRepository.count());
        assertTrue(alimentoRepository.findByCategoriaIsNull().isEmpty());
    }

    /** Simula o banco publicado na v1.0.0, em que os alimentos existem mas sem categoria. */
    @Test
    @Transactional
    void importacaoDeveCompletarCategoriasDeAlimentosJaCadastrados() {
        List<Alimento> alguns = alimentoRepository.findAllByOrderByIdAlimentoAsc().subList(0, 3);
        alguns.forEach(a -> a.setCategoria(null));
        alimentoRepository.saveAllAndFlush(alguns);
        assertEquals(3, alimentoRepository.findByCategoriaIsNull().size());

        tacoImportService.executar();

        assertTrue(alimentoRepository.findByCategoriaIsNull().isEmpty());
        assertEquals(597, alimentoRepository.count(), "não deve importar os alimentos de novo");
        assertEquals("Cereais e derivados", alguns.get(0).getCategoria().getNomeCategoria());
    }

    // ---------- busca ----------

    @Test
    void buscaSemAcentoDeveEncontrarFeijaoComCategoriaEValoresPor100g() throws Exception {
        mvc.perform(get("/alimentos").param("nome", "feijao carioca"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("max-age")))
                .andExpect(jsonPath("$[0].nomeAlimento").value("Feijão, carioca, cozido"))
                .andExpect(jsonPath("$[0].categoria").value("Leguminosas e derivados"))
                .andExpect(jsonPath("$[0].por100g.kcal").value(76.0))
                .andExpect(jsonPath("$[0].por100g.carboidratoG").value(13.6))
                .andExpect(jsonPath("$[0].por100g.proteinaG").value(4.8))
                .andExpect(jsonPath("$[0].por100g.gorduraG").value(0.5))
                .andExpect(jsonPath("$[0].por100g.sodioMg").value(2.0));
    }

    @Test
    void buscaDeveRespeitarOLimite() throws Exception {
        mvc.perform(get("/alimentos").param("nome", "arroz").param("limite", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/alimentos").param("nome", "a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(20)));
    }

    @Test
    void buscaComParametrosInvalidosDeveDevolver400Legivel() throws Exception {
        mvc.perform(get("/alimentos").param("nome", "arroz").param("limite", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem", containsString("limite")));
        mvc.perform(get("/alimentos").param("nome", "arroz").param("limite", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("limite")));
        mvc.perform(get("/alimentos"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("nome")));
        mvc.perform(get("/alimentos").param("nome", " "))
                .andExpect(status().isBadRequest());
    }

    // ---------- detalhe e catálogo ----------

    @Test
    void detalheDeveTrazerMicronutrientesComUnidade() throws Exception {
        int id = idDe("Arroz, integral, cozido");

        mvc.perform(get("/alimentos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("Cereais e derivados"))
                .andExpect(jsonPath("$.micros[?(@.nome == 'Sódio')].unidade").value(contains("mg")))
                .andExpect(jsonPath("$.micros[?(@.nome == 'Cálcio')].valorPor100g").value(contains(5.0)));
    }

    @Test
    void detalheDeAlimentoInexistenteDeveDevolver404() throws Exception {
        mvc.perform(get("/alimentos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void rotaInexistenteDeveDevolver404EmJson() throws Exception {
        mvc.perform(get("/rota-que-nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Rota não encontrada."));
    }

    @Test
    void catalogoJsonDeveTrazerTodosOsAlimentos() throws Exception {
        mvc.perform(get("/alimentos/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(597)))
                .andExpect(jsonPath("$[0].categoria").isNotEmpty());
    }

    @Test
    void catalogoCsvDeveTerCabecalhoEUmaLinhaPorAlimento() throws Exception {
        String csv = mvc.perform(get("/alimentos/catalogo/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("catalogo-taco.csv")))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        String[] linhas = csv.split("\n");
        assertEquals(598, linhas.length);
        assertTrue(linhas[0].startsWith("id_api_nutri,categoria,nome"));
        assertTrue(csv.contains("\"Feijão, carioca, cozido\",100 g,100,76.0,13.6,4.8,0.5,2.0"));
    }

    @Test
    void catalogoMySqlDeveTerUmInsertPorAlimento() throws Exception {
        String sql = mvc.perform(get("/alimentos/catalogo/mysql"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(".sql")))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertEquals(597, sql.split("INSERT INTO alimento \\(").length - 1);
        assertEquals(15, sql.split("INSERT INTO categoria \\(").length - 1);
        assertTrue(sql.contains("'Feijão, carioca, cozido', '100 g', 100, 76.0, 13.6, 4.8, 0.5, 2.0"));
    }

    // ---------- cálculo ----------

    @Test
    void calculoDeveSomarGordurasEInformarUnidades() throws Exception {
        int id = idDe("Arroz, integral, cozido");
        String corpo = "{\"itens\": [{\"idAlimento\": " + id + ", \"quantidadeGramas\": 200}]}";

        String json = mvc.perform(post("/calculo").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidadesMicros['Sódio']").value("mg"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        // TACO, arroz integral cozido por 100 g: 124 kcal, 1.0 g de lipídeos, 2.6 g de proteína
        assertEquals(248.0, numero(json, "$.totalCalorias"), 0.001);
        assertEquals(2.0, numero(json, "$.macros.gorduras"), 0.001);
        assertEquals(5.2, numero(json, "$.macros.proteinas"), 0.001);
        assertEquals(2.0, numero(json, "$.micros['Sódio']"), 0.001);
    }

    @Test
    void calculoComListaVaziaDeveDevolver400ComOCampo() throws Exception {
        mvc.perform(post("/calculo").contentType(MediaType.APPLICATION_JSON).content("{\"itens\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erros[0].campo").value("itens"))
                .andExpect(jsonPath("$.erros[0].mensagem").value("informe pelo menos um alimento"));
    }

    @Test
    void calculoComQuantidadeInvalidaDeveDevolver400ComOCampo() throws Exception {
        String corpo = "{\"itens\": [{\"idAlimento\": 1, \"quantidadeGramas\": -50}, {\"quantidadeGramas\": 100}]}";

        mvc.perform(post("/calculo").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo",
                        containsInAnyOrder("itens[0].quantidadeGramas", "itens[1].idAlimento")));
    }

    @Test
    void calculoSemCorpoOuComJsonInvalidoDeveDevolver400() throws Exception {
        mvc.perform(post("/calculo").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mvc.perform(post("/calculo").contentType(MediaType.APPLICATION_JSON).content("{\"itens\": [{\"quantidadeGramas\": \"muito\"}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculoComAlimentoInexistenteContinuaDevolvendo404() throws Exception {
        String corpo = "{\"itens\": [{\"idAlimento\": 999999, \"quantidadeGramas\": 100}]}";

        mvc.perform(post("/calculo").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isNotFound());
    }

    // ---------- CORS ----------

    @Test
    void corsDeveLiberarAsOrigensConfiguradas() throws Exception {
        mvc.perform(options("/alimentos")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));

        // a origem foi configurada com barra no final; a barra é ignorada
        mvc.perform(get("/alimentos").param("nome", "arroz")
                        .header(HttpHeaders.ORIGIN, "https://front.exemplo.com"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://front.exemplo.com"));
    }

    @Test
    void corsDeveBloquearOrigensNaoConfiguradas() throws Exception {
        mvc.perform(options("/calculo")
                        .header(HttpHeaders.ORIGIN, "https://site-qualquer.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

        mvc.perform(get("/alimentos").param("nome", "arroz")
                        .header(HttpHeaders.ORIGIN, "https://site-qualquer.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    void chamadaSemOrigemNaoEAfetadaPeloCors() throws Exception {
        mvc.perform(get("/alimentos").param("nome", "arroz"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    // ---------- apoio ----------

    private int idDe(String nome) {
        return alimentoRepository.findAllByOrderByIdAlimentoAsc().stream()
                .filter(a -> a.getNomeAlimento().equals(nome))
                .findFirst().orElseThrow()
                .getIdAlimento();
    }

    private static double numero(String json, String caminho) {
        return ((Number) JsonPath.read(json, caminho)).doubleValue();
    }

    private void cadastrarNutrientes() {
        for (String nome : List.of("Proteína", "Lipídeos", "Carboidrato")) {
            MacroNutriente macro = new MacroNutriente();
            macro.setNomeMacros(nome);
            macroNutrienteRepository.save(macro);
        }
        for (String nome : List.of("Colesterol", "Cálcio", "Magnésio", "Manganês", "Fósforo", "Ferro", "Sódio",
                "Potássio", "Cobre", "Zinco", "Vitamina A", "Tiamina", "Riboflavina", "Piridoxina", "Niacina",
                "Vitamina C")) {
            MicroNutriente micro = new MicroNutriente();
            micro.setNomeMicros(nome);
            micro.setUnidade(nome.equals("Vitamina A") ? "mcg" : "mg");
            microNutrienteRepository.save(micro);
        }
    }
}
