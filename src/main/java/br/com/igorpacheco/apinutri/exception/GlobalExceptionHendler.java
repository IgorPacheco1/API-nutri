package br.com.igorpacheco.apinutri.exception;

import br.com.igorpacheco.apinutri.dto.CampoInvalidoDTO;
import br.com.igorpacheco.apinutri.dto.ErroResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHendler {

    @ExceptionHandler(RecusoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> tratarNaoEncontrado(RecusoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ErroResponseDTO> tratarRequisicaoInvalida(RequisicaoInvalidaException ex) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Falhas de @Valid no corpo (ex.: lista vazia, quantidade negativa). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacao(MethodArgumentNotValidException ex) {
        List<CampoInvalidoDTO> erros = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> erros.add(new CampoInvalidoDTO(e.getField(), e.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors()
                .forEach(e -> erros.add(new CampoInvalidoDTO(e.getObjectName(), e.getDefaultMessage())));
        ErroResponseDTO corpo = ErroResponseDTO.de(HttpStatus.BAD_REQUEST.value(),
                "Requisição inválida: verifique os campos informados.", erros);
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> tratarCorpoIlegivel(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Corpo da requisição ausente ou em formato inválido. Envie um JSON como "
                        + "{\"itens\": [{\"idAlimento\": 1, \"quantidadeGramas\": 150}]}.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponseDTO> tratarParametroAusente(MissingServletRequestParameterException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente: '" + ex.getParameterName() + "'.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> tratarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + ex.getName() + "': '" + ex.getValue() + "'.");
    }

    /** Erros do próprio Spring MVC que já carregam o status certo (404, 405, 415). */
    @ExceptionHandler({
            NoResourceFoundException.class,
            NoHandlerFoundException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<ErroResponseDTO> tratarErroDoMvc(Exception ex) {
        HttpStatus status = HttpStatus.valueOf(((ErrorResponse) ex).getStatusCode().value());
        String mensagem = switch (status) {
            case NOT_FOUND -> "Rota não encontrada.";
            case METHOD_NOT_ALLOWED -> "Método HTTP não suportado nesta rota.";
            case UNSUPPORTED_MEDIA_TYPE -> "Tipo de conteúdo não suportado. Envie Content-Type: application/json.";
            default -> status.getReasonPhrase();
        };
        return resposta(status, mensagem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> tratarErroGenerico(Exception ex) {
        log.error("Erro inesperado", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado. Tente novamente mais tarde.");
    }

    private static ResponseEntity<ErroResponseDTO> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(ErroResponseDTO.de(status.value(), mensagem));
    }
}
