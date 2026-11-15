package com.renan.corebancario.dto;

import com.renan.corebancario.model.StatusCartao;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusCartaoDTO(
        @NotNull(message = "Informe o status do cartao")
        StatusCartao status
) {
}
