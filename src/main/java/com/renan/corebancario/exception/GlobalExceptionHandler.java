package com.renan.corebancario.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centraliza a traducao de exececoes em respostas HTTP consistentes,
 * evitando que os controllers fiquem poluidos com try/catch e garantindo
 * que o cliente da API sempre receba um corpo de erro no mesmo formato.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex,
                                                                     HttpServletRequest request) {
        log.warn("Recurso nao encontrado: {}", ex.getMessage());
        ErroResponse corpo = ErroResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Recurso nao encontrado",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo);
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErroResponse> tratarSaldoInsuficiente(SaldoInsuficienteException ex,
                                                                  HttpServletRequest request) {
        log.warn("Saldo insuficiente: {}", ex.getMessage());
        ErroResponse corpo = ErroResponse.of(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Saldo insuficiente",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(corpo);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraDeNegocio(RegraDeNegocioException ex,
                                                                HttpServletRequest request) {
        log.warn("Violacao de regra de negocio: {}", ex.getMessage());
        ErroResponse corpo = ErroResponse.of(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Regra de negocio violada",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(corpo);
    }

    /**
     * Erros de validacao do Bean Validation (@Valid) nos DTOs de entrada.
     * Retorna um mapa campo -> mensagem para facilitar o consumo pelo cliente.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarErrosDeValidacao(MethodArgumentNotValidException ex,
                                                                  HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                campos.put(erro.getField(), erro.getDefaultMessage())
        );

        ErroResponse corpo = ErroResponse.ofValidacao(
                HttpStatus.BAD_REQUEST.value(),
                "Dados invalidos",
                "Um ou mais campos nao passaram na validacao",
                request.getRequestURI(),
                campos
        );
        return ResponseEntity.badRequest().body(corpo);
    }

    /**
     * Violacoes de constraint de banco (ex.: unique key) que escapam da
     * validacao de negocio, geralmente por condicao de corrida.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> tratarViolacaoDeIntegridade(DataIntegrityViolationException ex,
                                                                       HttpServletRequest request) {
        log.error("Violacao de integridade de dados", ex);
        ErroResponse corpo = ErroResponse.of(
                HttpStatus.CONFLICT.value(),
                "Conflito de dados",
                "O registro viola uma restricao de unicidade ou integridade existente",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(corpo);
    }

    /**
     * Ultima linha de defesa: qualquer exececao nao tratada especificamente
     * cai aqui, evitando expor stacktraces ao cliente da API.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroGenerico(Exception ex, HttpServletRequest request) {
        log.error("Erro interno nao tratado", ex);
        ErroResponse corpo = ErroResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno",
                "Ocorreu um erro inesperado ao processar a requisicao",
                request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(corpo);
    }
}
