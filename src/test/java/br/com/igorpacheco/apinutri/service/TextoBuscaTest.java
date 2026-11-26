package br.com.igorpacheco.apinutri.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextoBuscaTest {

    @Test
    void deveRemoverAcentosMaiusculasEPontuacao() {
        assertEquals("feijao carioca cozido", TextoBusca.normalizar("Feijão, carioca, cozido"));
        assertEquals("pao de queijo", TextoBusca.normalizar("  PÃO   de queijo!! "));
        assertEquals("margarina sem sal 65 de lipideos",
                TextoBusca.normalizar("Margarina, sem sal (65% de lipídeos)"));
    }

    @Test
    void deveDevolverListaVaziaParaTextoSemLetrasOuNumeros() {
        assertEquals(List.of(), TextoBusca.palavras("  ,,, !! "));
        assertEquals(List.of(), TextoBusca.palavras(null));
    }

    @Test
    void cadaTermoPrecisaSerOComecoDeUmaPalavraDoNome() {
        List<String> nome = TextoBusca.palavras("Feijão, carioca, cozido");

        assertTrue(TextoBusca.contemTodos(nome, TextoBusca.palavras("feij")));
        assertTrue(TextoBusca.contemTodos(nome, TextoBusca.palavras("cozido feijao")));
        assertFalse(TextoBusca.contemTodos(nome, TextoBusca.palavras("ijao")));
        assertFalse(TextoBusca.contemTodos(nome, TextoBusca.palavras("feijao preto")));
    }
}
