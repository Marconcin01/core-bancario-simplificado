package com.renan.corebancario.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaldoResponseDTO(
        String numeroConta,
        BigDecimal saldoDisponivel,
        LocalDateTime consultadoEm
) {

    public static SaldoResponseDTO of(String numeroConta, BigDecimal saldo) {
        return new SaldoResponseDTO(numeroConta, saldo, LocalDateTime.now());
    }
}
