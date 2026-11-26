package br.com.igorpacheco.apinutri.service;

import br.com.igorpacheco.apinutri.dto.AlimentoDTO;
import br.com.igorpacheco.apinutri.dto.NutrientesPor100gDTO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Gera o catálogo em formatos prontos para carga em outro banco.
 * As colunas seguem a tabela "alimento" do PratoFácil (MySQL):
 * porcao_desc, porcao_g, kcal_100g, carb_100g, prot_100g, gord_100g (g) e sodio_100g_mg (mg),
 * com 1 casa decimal (cabem em DECIMAL(6,1) e DECIMAL(7,1)).
 */
@Service
public class CatalogoExportService {

    /** A TACO não traz porções caseiras; a porção de referência é 100 g. */
    public static final String PORCAO_DESC_PADRAO = "100 g";
    public static final int PORCAO_G_PADRAO = 100;

    static final String[] COLUNAS_CSV = {
            "id_api_nutri", "categoria", "nome", "porcao_desc", "porcao_g",
            "kcal_100g", "carb_100g", "prot_100g", "gord_100g", "sodio_100g_mg"
    };

    public String gerarCsv(List<AlimentoDTO> alimentos) {
        StringWriter saida = new StringWriter();
        CSVFormat formato = CSVFormat.DEFAULT.builder()
                .setHeader(COLUNAS_CSV)
                .setRecordSeparator("\n")
                .build();
        try (CSVPrinter csv = new CSVPrinter(saida, formato)) {
            for (AlimentoDTO alimento : alimentos) {
                NutrientesPor100gDTO n = alimento.por100g();
                csv.printRecord(
                        alimento.idAlimento(),
                        alimento.categoria(),
                        alimento.nomeAlimento(),
                        PORCAO_DESC_PADRAO,
                        PORCAO_G_PADRAO,
                        numeroCsv(n.kcal()),
                        numeroCsv(n.carboidratoG()),
                        numeroCsv(n.proteinaG()),
                        numeroCsv(n.gorduraG()),
                        numeroCsv(n.sodioMg()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return saida.toString();
    }

    /**
     * Script MySQL 8 para o banco do PratoFácil (tabelas "categoria" e "alimento").
     * Pode ser rodado mais de uma vez: não duplica categorias nem alimentos com o mesmo nome.
     */
    public String gerarScriptMySql(List<AlimentoDTO> alimentos) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
                -- =====================================================================
                -- Carga do catálogo da TACO no banco do PratoFácil (MySQL 8)
                -- Gerado pela API Nutricional (rota GET /alimentos/catalogo/mysql).
                --
                -- Valores por 100 g, com 1 casa decimal: kcal, carboidrato (g),
                -- proteína (g), gordura (g) e sódio (mg).
                -- NULL = nutriente não analisado na TACO ("NA").
                -- porcao_desc/porcao_g = '100 g'/100 (a TACO não traz porções caseiras).
                --
                -- Pode ser rodado mais de uma vez: categorias e alimentos que já
                -- existem com o mesmo nome não são inseridos de novo.
                -- Os IDs gerados aqui NÃO são os IDs da API (o ID da API fica no comentário "id_api_nutri").
                -- =====================================================================

                SET NAMES utf8mb4;
                START TRANSACTION;

                """);

        Set<String> categorias = new LinkedHashSet<>();
        for (AlimentoDTO alimento : alimentos) {
            if (alimento.categoria() != null) {
                categorias.add(alimento.categoria());
            }
        }
        sql.append("-- Categorias\n");
        for (String categoria : categorias) {
            String nome = texto(categoria);
            sql.append("INSERT INTO categoria (nome)\n")
                    .append("SELECT ").append(nome).append(" FROM DUAL\n")
                    .append("WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = ").append(nome).append(");\n");
        }

        sql.append("\n-- Alimentos\n");
        for (AlimentoDTO alimento : alimentos) {
            NutrientesPor100gDTO n = alimento.por100g();
            String nome = texto(alimento.nomeAlimento());
            String valores = String.join(", ",
                    nome,
                    texto(PORCAO_DESC_PADRAO),
                    String.valueOf(PORCAO_G_PADRAO),
                    numeroSql(n.kcal()),
                    numeroSql(n.carboidratoG()),
                    numeroSql(n.proteinaG()),
                    numeroSql(n.gorduraG()),
                    numeroSql(n.sodioMg()));

            sql.append("-- id_api_nutri ").append(alimento.idAlimento()).append('\n')
                    .append("INSERT INTO alimento (id_categoria, nome, porcao_desc, porcao_g, ")
                    .append("kcal_100g, carb_100g, prot_100g, gord_100g, sodio_100g_mg)\n");
            if (alimento.categoria() != null) {
                sql.append("SELECT c.id_categoria, ").append(valores).append('\n')
                        .append("FROM categoria c\n")
                        .append("WHERE c.nome = ").append(texto(alimento.categoria())).append('\n')
                        .append("  AND NOT EXISTS (SELECT 1 FROM alimento a WHERE a.nome = ").append(nome).append(");\n");
            } else {
                sql.append("SELECT NULL, ").append(valores).append(" FROM DUAL\n")
                        .append("WHERE NOT EXISTS (SELECT 1 FROM alimento a WHERE a.nome = ").append(nome).append(");\n");
            }
        }

        sql.append("\nCOMMIT;\n");
        return sql.toString();
    }

    private static String numeroCsv(BigDecimal valor) {
        return valor == null ? "" : valor.setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    private static String numeroSql(BigDecimal valor) {
        return valor == null ? "NULL" : valor.setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    /** Literal de texto do MySQL, com aspas simples e barras invertidas escapadas. */
    static String texto(String valor) {
        if (valor == null) {
            return "NULL";
        }
        return "'" + valor.replace("\\", "\\\\").replace("'", "''") + "'";
    }
}
