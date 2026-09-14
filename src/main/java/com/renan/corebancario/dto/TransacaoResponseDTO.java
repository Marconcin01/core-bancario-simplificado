package com.renan.corebancario.dto;

import com.renan.corebancario.model.Transacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransacaoResponseDTO(
        Long id,
        String tipo,
        BigDecimal valor,
        String contaOrigemNumero,
        String contaDestinoNumero,
        String descricao,
        LocalDateTime dataHora
) {

    public static TransacaoResponseDTO from(Transacao transacao) {
        return new TransacaoResponseDTO(
                transacao.getId(),
                transacao.getTipo().name(),
                transacao.getValor(),
                transacao.getContaOrigem() != null ? transacao.getContaOrigem().getNumero() : null,
                transacao.getContaDestino() != null ? transacao.getContaDestino().getNumero() : null,
                transacao.getDescricao(),
                transacao.getDataHora()
        );
    }
}
