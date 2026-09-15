package br.com.igorpacheco.apinutri.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ItemCalculoDTO(
        @Schema(description = "ID do alimento cadastrado", example = "1")
        Integer idAlimento,

        @Schema(description = "Quantidade em gramas", example = "150")
        BigDecimal quantidadeGramas
) {}
