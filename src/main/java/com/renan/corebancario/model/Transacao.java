package com.renan.corebancario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro imutavel de um lancamento no extrato de uma conta.
 *
 * Cada transferencia entre contas gera DOIS registros de Transacao (um de
 * debito na conta de origem e um de credito na conta de destino), o que
 * torna o extrato de cada conta independente e facil de consultar.
 */
@Entity
@Table(name = "transacoes", indexes = {
        @Index(name = "idx_transacao_conta_origem", columnList = "conta_origem_id"),
        @Index(name = "idx_transacao_conta_destino", columnList = "conta_destino_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoTransacao tipo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    /**
     * Conta a que este registro de extrato pertence (dono do lancamento).
     * E sempre preenchida: para um debito, e a conta que enviou o dinheiro;
     * para um credito, e a conta que recebeu.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    /**
     * Conta de origem da transferencia que gerou este lancamento.
     * Nula apenas para o lancamento de deposito inicial.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_origem_id")
    private Conta contaOrigem;

    /**
     * Conta de destino da transferencia que gerou este lancamento.
     * Nula apenas para o lancamento de deposito inicial.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_destino_id")
    private Conta contaDestino;

    @Column(length = 255)
    private String descricao;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @PrePersist
    protected void aoPersistir() {
        this.dataHora = LocalDateTime.now();
    }
}
