package br.com.igorpacheco.apinutri.exception;

public class RecusoNaoEncontradoException extends RuntimeException {
    public RecusoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
