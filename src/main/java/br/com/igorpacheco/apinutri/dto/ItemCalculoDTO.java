package br.com.igorpacheco.apinutri.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ItemCalculoDTO(
        @Schema(description = "ID do alimento cadastrado", example = "1")
        @NotNull(message = "informe o idAlimento")
        @Positive(message = "o idAlimento deve ser maior que zero")
        Integer idAlimento,

        @Schema(description = "Quantidade em gramas (maior que 0 e no máximo 10000)", example = "150")
        @NotNull(message = "informe a quantidadeGramas")
        @Positive(message = "a quantidadeGramas deve ser maior que zero")
        @DecimalMax(value = "10000", message = "a quantidadeGramas deve ser de no máximo 10000")
        BigDecimal quantidadeGramas
) {
}
