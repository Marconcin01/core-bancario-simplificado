package com.renan.corebancario.dto;

import com.renan.corebancario.model.Cartao;
import com.renan.corebancario.model.StatusCartao;

import java.time.format.DateTimeFormatter;

public record CartaoResponseDTO(
        Long id,
        String contaNumero,
        String clienteNome,
        String ultimosDigitos,
        String validade,
        String tipo,
        StatusCartao status
) {

    private static final DateTimeFormatter VALIDADE_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

    public static CartaoResponseDTO from(Cartao cartao) {
        return new CartaoResponseDTO(
                cartao.getId(),
                cartao.getConta().getNumero(),
                cartao.getConta().getCliente().getNome(),
                cartao.getUltimosDigitos(),
                cartao.getValidade().format(VALIDADE_FORMATTER),
                cartao.getTipo(),
                cartao.getStatus()
        );
    }
}
