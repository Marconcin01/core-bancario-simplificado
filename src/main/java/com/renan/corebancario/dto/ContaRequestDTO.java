package com.renan.corebancario.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Dados recebidos para abertura de uma nova conta para um cliente existente.
 */
public record ContaRequestDTO(

        @NotNull(message = "O id do cliente e obrigatorio")
        Long clienteId,

        @NotNull(message = "O saldo inicial e obrigatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "O saldo inicial nao pode ser negativo")
        BigDecimal saldoInicial
) {
}
