package com.renan.corebancario.repository;

import com.renan.corebancario.model.Transacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    /**
     * Extrato paginado de uma conta, ordenado por data/hora do mais recente
     * para o mais antigo (ordenacao aplicada via {@link Pageable} no service).
     */
    Page<Transacao> findByContaId(Long contaId, Pageable pageable);
}
