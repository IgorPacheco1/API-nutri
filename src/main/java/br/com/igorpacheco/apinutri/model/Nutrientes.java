package br.com.igorpacheco.apinutri.model;

import java.text.Normalizer;

/**
 * Nomes dos nutrientes exatamente como estão cadastrados em
 * macro_nutrientes e micro_nutrientes (ver database/schema.sql).
 * Usar sempre estas constantes evita erros como "Lipídios" x "Lipídeos".
 */
public final class Nutrientes {

    public static final String PROTEINA = "Proteína";
    public static final String LIPIDEOS = "Lipídeos";
    public static final String CARBOIDRATO = "Carboidrato";
    public static final String SODIO = "Sódio";

    private Nutrientes() {
    }

    /** Deixa o nome na forma Unicode NFC, para comparar acentos de forma segura. */
    public static String normalizar(String nome) {
        return nome == null ? "" : Normalizer.normalize(nome.trim(), Normalizer.Form.NFC);
    }
}
