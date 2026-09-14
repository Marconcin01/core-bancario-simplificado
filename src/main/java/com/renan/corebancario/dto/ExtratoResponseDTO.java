package com.renan.corebancario.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Extrato de uma conta: saldo atual + lancamentos de uma pagina do historico.
 */
public record ExtratoResponseDTO(
        String numeroConta,
        BigDecimal saldoAtual,
        int paginaAtual,
        int totalPaginas,
        long totalDeLancamentos,
        List<TransacaoResponseDTO> lancamentos
) {
}
