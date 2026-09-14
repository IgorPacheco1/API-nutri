package br.com.igorpacheco.apinutri.dto;

import java.math.BigDecimal;

public record ItemCalculoDTO(Integer idAlimento, BigDecimal quantidadeGramas) {
}
