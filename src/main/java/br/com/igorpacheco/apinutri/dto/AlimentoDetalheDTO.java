package br.com.igorpacheco.apinutri.dto;

import java.util.List;

public record AlimentoDetalheDTO(
        Integer idAlimento,
        String nomeAlimento,
        String categoria,
        NutrientesPor100gDTO por100g,
        List<MicroNutrienteDTO> micros
) {
}
