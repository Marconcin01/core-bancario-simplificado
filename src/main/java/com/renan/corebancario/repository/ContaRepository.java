package com.renan.corebancario.repository;

import com.renan.corebancario.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    Optional<Conta> findByNumero(String numero);

    boolean existsByNumero(String numero);

    List<Conta> findByClienteId(Long clienteId);

    /**
     * Busca a conta aplicando um lock pessimista de escrita (SELECT ... FOR UPDATE).
     *
     * Em uma transferencia entre contas, duas requisicoes concorrentes podem
     * tentar debitar/creditar a mesma conta ao mesmo tempo. Sem um lock, ambas
     * poderiam ler o mesmo saldo antes de qualquer uma commitar, permitindo
     * uma condicao de corrida que deixaria o saldo inconsistente (ex.: saque
     * em duplicidade). O lock forca as transacoes concorrentes sobre a mesma
     * conta a serem serializadas.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conta c where c.id = :id")
    Optional<Conta> buscarPorIdComLock(Long id);
}
