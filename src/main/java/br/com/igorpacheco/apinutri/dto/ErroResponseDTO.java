package br.com.igorpacheco.apinutri.dto;

import java.time.LocalDateTime;

public record ErroResponseDTO(int status,
                              String mensagem,
                              LocalDateTime timestamp) {
}
