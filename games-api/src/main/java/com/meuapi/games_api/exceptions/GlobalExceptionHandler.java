package com.meuapi.games_api.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            RecursoNaoEncontradoException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(NaoAutorizadoException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            NaoAutorizadoException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request,
                List.of("Informe uma chave valida no header X-API-Key"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatarErroCampo)
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Dados invalidos na requisicao", request, detalhes);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            InvalidDataAccessApiUsageException.class,
            PropertyReferenceException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Nao foi possivel processar a requisicao", request,
                List.of(mensagemSegura(ex)));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "Conflito com dados ja cadastrados", request,
                List.of("Verifique campos unicos, como e-mail de usuario ou chave de API."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleJsonInvalido(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, "JSON invalido ou mal formatado", request,
                List.of("Confira a sintaxe do JSON e os valores enviados nos campos."));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaType(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type nao suportado", request,
                List.of("Use Content-Type: application/json para enviar dados em JSON."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        String metodos = ex.getSupportedHttpMethods() == null
                ? "Consulte a documentacao do endpoint."
                : "Metodos permitidos: " + ex.getSupportedHttpMethods();
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Metodo HTTP nao permitido para este endpoint", request,
                List.of(metodos));
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleEndpointNotFound(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Endpoint nao encontrado", request,
                List.of("Confira o caminho da URL e consulte /swagger-ui/index.html."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", request,
                List.of("Se o problema persistir, revise os dados enviados e os logs da aplicacao."));
    }

    private String formatarErroCampo(FieldError erro) {
        return erro.getField() + ": " + erro.getDefaultMessage();
    }

    private String mensagemSegura(Exception ex) {
        return ex.getMessage() == null ? "Requisicao invalida" : ex.getMessage();
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String mensagem,
            HttpServletRequest request,
            List<String> detalhes
    ) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(
                        LocalDateTime.now().toString(),
                        status.value(),
                        status.getReasonPhrase(),
                        mensagem,
                        request.getRequestURI(),
                        request.getMethod(),
                        detalhes
                ));
    }
}
