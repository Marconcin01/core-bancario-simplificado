package com.renan.corebancario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChavePixRequestDTO(
        @NotBlank(message = "A chave Pix e obrigatoria")
        @Size(max = 100, message = "A chave Pix deve ter no maximo 100 caracteres")
        String chavePix
) {
}
