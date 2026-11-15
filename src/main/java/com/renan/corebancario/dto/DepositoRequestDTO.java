package com.renan.corebancario.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositoRequestDTO(
        @NotNull(message = "O valor do deposito e obrigatorio")
        @DecimalMin(value = "0.01", message = "O valor do deposito deve ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "Informe se o deposito foi recebido por Pix ou TED")
        MeioDeposito meio
) {
    public enum MeioDeposito {
        PIX,
        TED
    }
}
