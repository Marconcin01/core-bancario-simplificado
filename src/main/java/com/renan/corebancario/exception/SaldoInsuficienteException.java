package com.renan.corebancario.exception;

/**
 * Lancada quando uma conta nao possui saldo disponivel suficiente
 * para concluir uma transferencia/debito.
 */
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
