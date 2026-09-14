package com.renan.corebancario.dto;

import com.renan.corebancario.model.Conta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ContaResponseDTO(
        Long id,
        String numero,
        BigDecimal saldo,
        Long clienteId,
        String nomeCliente,
        LocalDateTime dataCriacao
) {

    public static ContaResponseDTO from(Conta conta) {
        return new ContaResponseDTO(
                conta.getId(),
                conta.getNumero(),
                conta.getSaldo(),
                conta.getCliente().getId(),
                conta.getCliente().getNome(),
                conta.getDataCriacao()
        );
    }
}
