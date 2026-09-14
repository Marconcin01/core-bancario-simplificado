package com.renan.corebancario.exception;

/**
 * Exececao generica para violacoes de regra de negocio que nao se encaixam
 * nas exececoes mais especificas (ex.: transferir para a mesma conta,
 * CPF/email ja cadastrado, etc.).
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
