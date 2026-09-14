package com.renan.corebancario.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa o cliente (correntista) do banco.
 * Um cliente pode possuir uma ou mais contas.
 */
@Entity
@Table(name = "clientes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cliente_cpf", columnNames = "cpf"),
        @UniqueConstraint(name = "uk_cliente_email", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    /**
     * Guardado apenas com os digitos (sem mascara) para simplificar
     * unicidade e comparacao. A validacao de formato acontece na camada de DTO.
     */
    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(nullable = false, length = 150)
    private String email;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Conta> contas = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void aoPersistir() {
        this.dataCriacao = LocalDateTime.now();
    }
}
