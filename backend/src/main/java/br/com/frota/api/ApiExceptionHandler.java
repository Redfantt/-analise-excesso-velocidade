package br.com.frota.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record Erro(String mensagem) {}

    @ExceptionHandler(ArquivoInvalidoException.class)
    public ResponseEntity<Erro> arquivoInvalido(ArquivoInvalidoException erro) {
        return ResponseEntity.badRequest().body(new Erro(erro.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Erro> arquivoAusente() {
        return ResponseEntity.badRequest().body(new Erro("Envie o arquivo no campo multipart 'arquivo'."));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Erro> arquivoGrande() {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new Erro("O arquivo excede o limite de 5 MB."));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Erro> tipoInvalido() {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(new Erro("Envie o arquivo usando multipart/form-data."));
    }
}
