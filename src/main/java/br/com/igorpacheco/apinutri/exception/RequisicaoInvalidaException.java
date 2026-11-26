package br.com.igorpacheco.apinutri.exception;

/** Erro de entrada do cliente: vira resposta HTTP 400 com a mensagem informada. */
public class RequisicaoInvalidaException extends RuntimeException {
    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
