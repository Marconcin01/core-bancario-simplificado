package com.renan.corebancario.model;

/**
 * Tipos de lancamento que compoem o extrato de uma conta.
 */
public enum TipoTransacao {

    /** Credito recebido de uma transferencia originada em outra conta. */
    TRANSFERENCIA_CREDITO,

    /** Debito referente a uma transferencia enviada para outra conta. */
    TRANSFERENCIA_DEBITO,

    /** Lancamento de credito referente ao deposito do saldo inicial da conta. */
    DEPOSITO_INICIAL
}
