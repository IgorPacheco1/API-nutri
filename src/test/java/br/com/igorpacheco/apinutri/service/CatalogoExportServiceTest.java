package br.com.igorpacheco.apinutri.service;

import br.com.igorpacheco.apinutri.dto.AlimentoDTO;
import br.com.igorpacheco.apinutri.dto.NutrientesPor100gDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoExportServiceTest {

    private final CatalogoExportService service = new CatalogoExportService();

    private static final AlimentoDTO ARROZ = new AlimentoDTO(1, "Arroz, integral, cozido", "Cereais e derivados",
            new NutrientesPor100gDTO(new BigDecimal("124.0"), new BigDecimal("25.8"), new BigDecimal("2.6"),
                    new BigDecimal("1.0"), new BigDecimal("1.0")));

    private static final AlimentoDTO ARROZ_CRU = new AlimentoDTO(2, "Arroz, integral, cru", "Cereais e derivados",
            new NutrientesPor100gDTO(new BigDecimal("360.0"), new BigDecimal("77.5"), new BigDecimal("7.3"),
                    new BigDecimal("1.9"), new BigDecimal("2.0")));

    private static final AlimentoDTO SAL = new AlimentoDTO(517, "Sal, grosso", "Miscelâneas",
            new NutrientesPor100gDTO(null, null, null, null, new BigDecimal("39943.0")));

    @Test
    void csvDeveTerCabecalhoComAsColunasDoPratoFacil() {
        String csv = service.gerarCsv(List.of(ARROZ));
        String[] linhas = csv.split("\n");

        assertEquals("id_api_nutri,categoria,nome,porcao_desc,porcao_g,kcal_100g,carb_100g,prot_100g,gord_100g,sodio_100g_mg",
                linhas[0]);
        assertEquals("1,Cereais e derivados,\"Arroz, integral, cozido\",100 g,100,124.0,25.8,2.6,1.0,1.0", linhas[1]);
    }

    @Test
    void csvDeveDeixarVazioONutrienteNaoAnalisado() {
        String csv = service.gerarCsv(List.of(SAL));

        assertEquals("517,Miscelâneas,\"Sal, grosso\",100 g,100,,,,,39943.0", csv.split("\n")[1]);
    }

    @Test
    void scriptMySqlDeveInserirCadaCategoriaUmaVezSemDuplicar() {
        String sql = service.gerarScriptMySql(List.of(ARROZ, ARROZ_CRU, SAL));

        assertEquals(1, ocorrencias(sql, "SELECT 'Cereais e derivados' FROM DUAL"));
        assertEquals(1, ocorrencias(sql, "SELECT 'Miscelâneas' FROM DUAL"));
        assertTrue(sql.contains("WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Cereais e derivados');"));
    }

    @Test
    void scriptMySqlDeveInserirOsAlimentosComValoresPor100gENull() {
        String sql = service.gerarScriptMySql(List.of(ARROZ, SAL));

        assertTrue(sql.startsWith("-- ="));
        assertTrue(sql.contains("SET NAMES utf8mb4;\nSTART TRANSACTION;"));
        assertTrue(sql.trim().endsWith("COMMIT;"));
        assertEquals(2, ocorrencias(sql, "INSERT INTO alimento (id_categoria, nome, porcao_desc, porcao_g, "
                + "kcal_100g, carb_100g, prot_100g, gord_100g, sodio_100g_mg)"));
        assertTrue(sql.contains(
                "SELECT c.id_categoria, 'Arroz, integral, cozido', '100 g', 100, 124.0, 25.8, 2.6, 1.0, 1.0\n"
                        + "FROM categoria c\n"
                        + "WHERE c.nome = 'Cereais e derivados'\n"
                        + "  AND NOT EXISTS (SELECT 1 FROM alimento a WHERE a.nome = 'Arroz, integral, cozido');"));
        assertTrue(sql.contains("'Sal, grosso', '100 g', 100, NULL, NULL, NULL, NULL, 39943.0"));
        assertTrue(sql.contains("-- id_api_nutri 517"));
    }

    @Test
    void scriptMySqlDeveEscaparAspasDoNome() {
        AlimentoDTO comAspas = new AlimentoDTO(9, "Pão d'água", null,
                new NutrientesPor100gDTO(new BigDecimal("1.0"), null, null, null, null));

        String sql = service.gerarScriptMySql(List.of(comAspas));

        assertTrue(sql.contains("'Pão d''água'"));
        assertTrue(sql.contains("SELECT NULL, 'Pão d''água', '100 g', 100, 1.0, NULL, NULL, NULL, NULL FROM DUAL"));
        assertEquals("'a\\\\b'", CatalogoExportService.texto("a\\b"));
    }

    private static int ocorrencias(String texto, String trecho) {
        int total = 0;
        int indice = texto.indexOf(trecho);
        while (indice >= 0) {
            total++;
            indice = texto.indexOf(trecho, indice + trecho.length());
        }
        return total;
    }
}
