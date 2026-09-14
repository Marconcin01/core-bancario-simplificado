package com.renan.corebancario.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferenciaResponseDTO(
        String mensagem,
        String numeroContaOrigem,
        String numeroContaDestino,
        BigDecimal valorTransferido,
        BigDecimal saldoAtualContaOrigem,
        LocalDateTime dataHora
) {
}
