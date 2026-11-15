package com.renan.corebancario.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Dados recebidos para realizar uma transferencia entre duas contas.
 */
public record TransferenciaRequestDTO(

        @NotBlank(message = "O numero da conta de origem e obrigatorio")
        String numeroContaOrigem,

        String numeroContaDestino,

        String chavePixDestino,

        @NotNull(message = "O valor da transferencia e obrigatorio")
        @DecimalMin(value = "0.01", message = "O valor da transferencia deve ser maior que zero")
        BigDecimal valor
) {
    public TransferenciaRequestDTO(String numeroContaOrigem, String numeroContaDestino, BigDecimal valor) {
        this(numeroContaOrigem, numeroContaDestino, null, valor);
    }
}
