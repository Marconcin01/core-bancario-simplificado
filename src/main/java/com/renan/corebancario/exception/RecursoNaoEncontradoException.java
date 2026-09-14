package com.renan.corebancario.exception;

/**
 * Lancada quando um Cliente, Conta ou outro recurso buscado por id/numero
 * nao e encontrado na base de dados.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
