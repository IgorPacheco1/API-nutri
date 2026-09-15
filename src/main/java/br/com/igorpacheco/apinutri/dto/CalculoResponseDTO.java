package br.com.igorpacheco.apinutri.dto;

import java.math.BigDecimal;
import java.util.Map;

public record CalculoResponseDTO(BigDecimal totalCalorias, MacrosDTO macros, Map<String, BigDecimal> micros) {
}
