package com.renan.corebancario.dto;

import com.renan.corebancario.model.Cliente;

import java.time.LocalDateTime;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String cpf,
        String email,
        LocalDateTime dataCriacao
) {

    public static ClienteResponseDTO from(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getEmail(),
                cliente.getDataCriacao()
        );
    }
}
