package com.renan.corebancario.service;

import com.renan.corebancario.dto.CartaoResponseDTO;
import com.renan.corebancario.exception.RecursoNaoEncontradoException;
import com.renan.corebancario.model.Cartao;
import com.renan.corebancario.model.Conta;
import com.renan.corebancario.model.StatusCartao;
import com.renan.corebancario.repository.CartaoRepository;
import com.renan.corebancario.repository.ContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartaoService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final CartaoRepository cartaoRepository;
    private final ContaRepository contaRepository;

    public List<CartaoResponseDTO> listarPorConta(String numeroConta) {
        buscarContaOuFalhar(numeroConta);
        return cartaoRepository.findByContaNumeroOrderByDataCriacaoDesc(numeroConta)
                .stream()
                .map(CartaoResponseDTO::from)
                .toList();
    }

    @Transactional
    public CartaoResponseDTO criar(String numeroConta) {
        Conta conta = buscarContaOuFalhar(numeroConta);
        YearMonth validade = YearMonth.now().plusYears(4);

        Cartao cartao = Cartao.builder()
                .conta(conta)
                .ultimosDigitos(String.format("%04d", RANDOM.nextInt(10_000)))
                .tipo("DEBITO_VIRTUAL")
                .status(StatusCartao.ATIVO)
                .validade(validade.atEndOfMonth())
                .build();

        return CartaoResponseDTO.from(cartaoRepository.save(cartao));
    }

    @Transactional
    public CartaoResponseDTO atualizarStatus(Long cartaoId, StatusCartao status) {
        Cartao cartao = cartaoRepository.findById(cartaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cartao nao encontrado com id: " + cartaoId));
        cartao.setStatus(status);
        return CartaoResponseDTO.from(cartaoRepository.save(cartao));
    }

    private Conta buscarContaOuFalhar(String numeroConta) {
        return contaRepository.findByNumero(numeroConta)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Conta nao encontrada com numero: " + numeroConta));
    }
}
