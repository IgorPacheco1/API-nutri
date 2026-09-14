package br.com.igorpacheco.apinutri.dto;

import java.math.BigDecimal;

public record CalculoResponseDTO(BigDecimal totalCalorias, MacrosDTO macros) {
}
