package br.com.igorpacheco.apinutri.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CalculoRequestDTO(
        @NotEmpty(message = "informe pelo menos um alimento")
        @Size(max = 100, message = "informe no máximo 100 alimentos por cálculo")
        List<@NotNull(message = "o item não pode ser nulo") @Valid ItemCalculoDTO> itens
) {
}
