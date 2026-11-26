package br.com.igorpacheco.apinutri.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record MicroNutrienteDTO(
        @Schema(example = "Cálcio") String nome,
        @Schema(description = "Valor por 100 g, com 2 casas decimais", example = "5.00") BigDecimal valorPor100g,
        @Schema(description = "\"mg\" ou \"mcg\"", example = "mg") String unidade
) {
}
