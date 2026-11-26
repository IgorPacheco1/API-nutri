package br.com.igorpacheco.apinutri.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Corpo padrão de erro. Os campos status, mensagem e timestamp existem desde a v1.0.0;
 * "erros" (v1.1.0) lista os campos inválidos quando o erro é de validação.
 */
public record ErroResponseDTO(int status,
                              String mensagem,
                              LocalDateTime timestamp,
                              List<CampoInvalidoDTO> erros) {

    public static ErroResponseDTO de(int status, String mensagem) {
        return new ErroResponseDTO(status, mensagem, LocalDateTime.now(), List.of());
    }

    public static ErroResponseDTO de(int status, String mensagem, List<CampoInvalidoDTO> erros) {
        return new ErroResponseDTO(status, mensagem, LocalDateTime.now(), erros);
    }
}
