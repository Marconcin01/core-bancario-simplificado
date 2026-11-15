package com.renan.corebancario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cartoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @Column(name = "ultimos_digitos", nullable = false, length = 4)
    private String ultimosDigitos;

    @Column(nullable = false, length = 24)
    @Builder.Default
    private String tipo = "DEBITO_VIRTUAL";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private StatusCartao status = StatusCartao.ATIVO;

    @Column(nullable = false)
    private LocalDate validade;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void aoPersistir() {
        this.dataCriacao = LocalDateTime.now();
    }
}
