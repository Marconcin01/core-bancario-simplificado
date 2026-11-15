package com.renan.corebancario.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DepositoResponseDTO(
        String mensagem,
        String numeroConta,
        DepositoRequestDTO.MeioDeposito meio,
        BigDecimal valorDepositado,
        BigDecimal saldoAtual,
        LocalDateTime dataHora
) {
}
