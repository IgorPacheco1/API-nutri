package br.com.igorpacheco.apinutri.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

public record CalculoResponseDTO(
        @Schema(description = "Total de energia, em kcal")
        BigDecimal totalCalorias,

        @Schema(description = "Totais de proteínas, carboidratos e gorduras, em gramas")
        MacrosDTO macros,

        @Schema(description = "Total de cada micronutriente, na unidade indicada em unidadesMicros")
        Map<String, BigDecimal> micros,

        @Schema(description = "Unidade de cada micronutriente de \"micros\" (\"mg\" ou \"mcg\"). Novo na v1.1.0.",
                example = "{\"Sódio\": \"mg\", \"Vitamina A\": \"mcg\"}")
        Map<String, String> unidadesMicros
) {
}
