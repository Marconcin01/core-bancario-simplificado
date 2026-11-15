package com.renan.corebancario.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma conta bancaria vinculada a um {@link Cliente}.
 *
 * O saldo e mantido diretamente na conta (em vez de ser somado a partir do
 * historico de transacoes a cada consulta) porque, em um core bancario, a
 * leitura de saldo precisa ser rapida e consistente; a integridade desse
 * saldo e garantida pelas operacoes transacionais da camada de servico.
 */
@Entity
@Table(name = "contas", uniqueConstraints = {
        @UniqueConstraint(name = "uk_conta_numero", columnNames = "numero"),
        @UniqueConstraint(name = "uk_conta_chave_pix", columnNames = "chave_pix")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Numero de conta gerado pelo sistema (sequencial com padding),
     * exposto ao cliente para uso em transferencias.
     */
    @Column(nullable = false, length = 20, unique = true)
    private String numero;

    @Column(name = "chave_pix", length = 100)
    private String chavePix;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "contaOrigem")
    @JsonIgnore
    @Builder.Default
    private List<Transacao> transacoesEnviadas = new ArrayList<>();

    @OneToMany(mappedBy = "contaDestino")
    @JsonIgnore
    @Builder.Default
    private List<Transacao> transacoesRecebidas = new ArrayList<>();

    @Version
    private Long versao;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void aoPersistir() {
        this.dataCriacao = LocalDateTime.now();
    }

    /**
     * Debita o valor informado do saldo da conta.
     * Nao faz a validacao de saldo suficiente: essa regra de negocio
     * pertence a camada de servico (ContaService/TransferenciaService),
     * que e quem decide o que fazer quando o saldo e insuficiente.
     */
    public void debitar(BigDecimal valor) {
        this.saldo = this.saldo.subtract(valor);
    }

    /**
     * Credita o valor informado no saldo da conta.
     */
    public void creditar(BigDecimal valor) {
        this.saldo = this.saldo.add(valor);
    }

    /**
     * Verifica se a conta possui saldo disponivel igual ou maior que o valor informado.
     */
    public boolean possuiSaldoSuficiente(BigDecimal valor) {
        return this.saldo.compareTo(valor) >= 0;
    }
}
