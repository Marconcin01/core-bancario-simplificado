package com.renan.corebancario.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato padronizado de corpo de resposta para todos os erros da API.
 */
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String path,
        Map<String, String> camposInvalidos
) {

    public static ErroResponse of(int status, String erro, String mensagem, String path) {
        return new ErroResponse(LocalDateTime.now(), status, erro, mensagem, path, null);
    }

    public static ErroResponse ofValidacao(int status, String erro, String mensagem, String path,
                                            Map<String, String> camposInvalidos) {
        return new ErroResponse(LocalDateTime.now(), status, erro, mensagem, path, camposInvalidos);
    }
}
