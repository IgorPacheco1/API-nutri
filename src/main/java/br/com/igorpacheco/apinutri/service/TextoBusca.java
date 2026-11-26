package br.com.igorpacheco.apinutri.service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normalização de texto para a busca: sem acentos, minúsculo e sem pontuação.
 * Ex.: "Feijão, carioca, cozido" vira as palavras [feijao, carioca, cozido].
 */
public final class TextoBusca {

    private static final Pattern ACENTOS = Pattern.compile("\\p{M}+");
    private static final Pattern NAO_ALFANUMERICO = Pattern.compile("[^a-z0-9]+");

    private TextoBusca() {
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = ACENTOS.matcher(Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");
        String minusculo = semAcento.toLowerCase(Locale.ROOT);
        return NAO_ALFANUMERICO.matcher(minusculo).replaceAll(" ").trim();
    }

    public static List<String> palavras(String texto) {
        String normalizado = normalizar(texto);
        if (normalizado.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(normalizado.split(" "));
    }

    /**
     * Cada termo digitado precisa ser o começo de alguma palavra do nome.
     * Assim "feij" já encontra "Feijão" enquanto a pessoa digita, e
     * "arroz integral" encontra "Arroz, integral, cozido".
     */
    public static boolean contemTodos(List<String> palavrasDoNome, List<String> termos) {
        for (String termo : termos) {
            boolean achou = false;
            for (String palavra : palavrasDoNome) {
                if (palavra.startsWith(termo)) {
                    achou = true;
                    break;
                }
            }
            if (!achou) {
                return false;
            }
        }
        return true;
    }
}
