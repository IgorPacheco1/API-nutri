package br.com.igorpacheco.apinutri.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Alimento com categoria e valores por 100 g, suficientes para o cliente "
        + "calcular qualquer porção sem nova chamada (valor × gramas ÷ 100).")
public record AlimentoDTO(
        @Schema(description = "ID do alimento nesta API (não é o ID do catálogo do PratoFácil)", example = "1")
        Integer idAlimento,

        @Schema(description = "Nome do alimento como está na TACO", example = "Arroz, integral, cozido")
        String nomeAlimento,

        @Schema(description = "Categoria da TACO", example = "Cereais e derivados")
        String categoria,

        NutrientesPor100gDTO por100g
) {
}
