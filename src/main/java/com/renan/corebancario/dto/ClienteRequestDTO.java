package com.renan.corebancario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Dados recebidos para cadastro de um novo cliente.
 * Usar um record deixa o DTO imutavel e conciso (getters gerados automaticamente).
 */
public record ClienteRequestDTO(

        @NotBlank(message = "O nome e obrigatorio")
        String nome,

        @NotBlank(message = "O CPF e obrigatorio")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 digitos numericos, sem pontuacao")
        String cpf,

        @NotBlank(message = "O email e obrigatorio")
        @Email(message = "Informe um email valido")
        String email
) {
}
