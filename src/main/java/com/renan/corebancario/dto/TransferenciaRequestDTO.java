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

        @NotBlank(message = "O numero da conta de destino e obrigatorio")
        String numeroContaDestino,

        @NotNull(message = "O valor da transferencia e obrigatorio")
        @DecimalMin(value = "0.01", message = "O valor da transferencia deve ser maior que zero")
        BigDecimal valor
) {
}
