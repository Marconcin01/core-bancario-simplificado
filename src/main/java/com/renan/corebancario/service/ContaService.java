package com.renan.corebancario.service;

import com.renan.corebancario.dto.*;
import com.renan.corebancario.exception.RecursoNaoEncontradoException;
import com.renan.corebancario.model.Cliente;
import com.renan.corebancario.model.Conta;
import com.renan.corebancario.model.Transacao;
import com.renan.corebancario.model.TipoTransacao;
import com.renan.corebancario.repository.ContaRepository;
import com.renan.corebancario.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

/**
 * Regras de negocio relacionadas a contas: abertura, consulta de saldo e extrato.
 * A logica de transferencia entre contas fica isolada em {@link TransferenciaService},
 * para manter esta classe focada no ciclo de vida da propria conta (SRP).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContaService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final ClienteService clienteService;

    @Transactional
    public ContaResponseDTO abrirConta(ContaRequestDTO dto) {
        Cliente cliente = clienteService.buscarClienteOuFalhar(dto.clienteId());

        Conta conta = Conta.builder()
                .numero(gerarNumeroDeContaUnico())
                .saldo(dto.saldoInicial())
                .cliente(cliente)
                .build();

        Conta contaSalva = contaRepository.save(conta);

        if (dto.saldoInicial().signum() > 0) {
            registrarLancamento(contaSalva, TipoTransacao.DEPOSITO_INICIAL, dto.saldoInicial(),
                    null, null, "Deposito de saldo inicial na abertura da conta");
        }

        return ContaResponseDTO.from(contaSalva);
    }

    public ContaResponseDTO buscarPorId(Long id) {
        return ContaResponseDTO.from(buscarContaOuFalhar(id));
    }

    public List<ContaResponseDTO> listarPorCliente(Long clienteId) {
        return contaRepository.findByClienteId(clienteId)
                .stream()
                .map(ContaResponseDTO::from)
                .toList();
    }

    public SaldoResponseDTO consultarSaldo(String numeroConta) {
        Conta conta = buscarContaOuFalhar(numeroConta);
        return SaldoResponseDTO.of(conta.getNumero(), conta.getSaldo());
    }

    public ExtratoResponseDTO consultarExtrato(String numeroConta, int pagina, int tamanhoPagina) {
        Conta conta = buscarContaOuFalhar(numeroConta);

        PageRequest paginacao = PageRequest.of(pagina, tamanhoPagina, Sort.by("dataHora").descending());
        Page<Transacao> paginaDeTransacoes = transacaoRepository.findByContaId(conta.getId(), paginacao);

        List<TransacaoResponseDTO> lancamentos = paginaDeTransacoes.getContent()
                .stream()
                .map(TransacaoResponseDTO::from)
                .toList();

        return new ExtratoResponseDTO(
                conta.getNumero(),
                conta.getSaldo(),
                paginaDeTransacoes.getNumber(),
                paginaDeTransacoes.getTotalPages(),
                paginaDeTransacoes.getTotalElements(),
                lancamentos
        );
    }

    protected Conta buscarContaOuFalhar(Long id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta nao encontrada com id: " + id));
    }

    protected Conta buscarContaOuFalhar(String numero) {
        return contaRepository.findByNumero(numero)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta nao encontrada com numero: " + numero));
    }

    /**
     * Cria o lancamento de extrato correspondente a uma operacao na conta.
     * Reaproveitado tanto no deposito inicial quanto nas transferencias.
     */
    protected void registrarLancamento(Conta conta, TipoTransacao tipo, BigDecimal valor,
                                         Conta contaOrigem, Conta contaDestino, String descricao) {
        Transacao transacao = Transacao.builder()
                .conta(conta)
                .tipo(tipo)
                .valor(valor)
                .contaOrigem(contaOrigem)
                .contaDestino(contaDestino)
                .descricao(descricao)
                .build();
        transacaoRepository.save(transacao);
    }

    /**
     * Gera um numero de conta sequencial aleatorio de 8 digitos e garante,
     * consultando o banco, que ele ainda nao esta em uso.
     */
    private String gerarNumeroDeContaUnico() {
        String numero;
        do {
            numero = String.valueOf(10_000_000 + RANDOM.nextInt(90_000_000));
        } while (contaRepository.existsByNumero(numero));
        return numero;
    }
}
