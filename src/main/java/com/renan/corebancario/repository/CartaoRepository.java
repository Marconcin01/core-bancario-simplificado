package com.renan.corebancario.repository;

import com.renan.corebancario.model.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartaoRepository extends JpaRepository<Cartao, Long> {

    List<Cartao> findByContaNumeroOrderByDataCriacaoDesc(String numeroConta);
}
