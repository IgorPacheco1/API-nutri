package br.com.igorpacheco.apinutri.dto;

import java.util.List;

public record CalculoRequestDTO(List<ItemCalculoDTO> itens) {
}
