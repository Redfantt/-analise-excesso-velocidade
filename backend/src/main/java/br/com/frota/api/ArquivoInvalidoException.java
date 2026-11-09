package br.com.frota.api;

public class ArquivoInvalidoException extends RuntimeException {
    public ArquivoInvalidoException(String mensagem) { super(mensagem); }
}
