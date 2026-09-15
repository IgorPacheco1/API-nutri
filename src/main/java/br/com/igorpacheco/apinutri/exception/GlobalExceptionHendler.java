
package br.com.igorpacheco.apinutri.exception;

import br.com.igorpacheco.apinutri.dto.ErroResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHendler {

    @ExceptionHandler(RecusoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> tratarNaoEncontrado(RecusoNaoEncontradoException ex) {
        ErroResponseDTO erro = new ErroResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> tratarErroGenerico(Exception ex) {
        ErroResponseDTO erro = new ErroResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}