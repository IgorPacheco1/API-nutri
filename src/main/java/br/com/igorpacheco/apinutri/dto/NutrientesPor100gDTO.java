package br.com.igorpacheco.apinutri.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Valores por 100 g do alimento, com 1 casa decimal. "
        + "null = nutriente não analisado na TACO (\"NA\"); traços (\"Tr\") viram 0.0.")
public record NutrientesPor100gDTO(
        @Schema(description = "Energia, em kcal", example = "124.0")
        BigDecimal kcal,

        @Schema(description = "Carboidrato, em gramas", example = "25.8")
        BigDecimal carboidratoG,

        @Schema(description = "Proteína, em gramas", example = "2.6")
        BigDecimal proteinaG,

        @Schema(description = "Gordura total (lipídeos), em gramas", example = "1.0")
        BigDecimal gorduraG,

        @Schema(description = "Sódio, em miligramas", example = "1.0")
        BigDecimal sodioMg
) {
}
