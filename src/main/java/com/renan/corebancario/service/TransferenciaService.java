package com.renan.corebancario.service;

import com.renan.corebancario.dto.TransferenciaRequestDTO;
import com.renan.corebancario.dto.TransferenciaResponseDTO;
import com.renan.corebancario.exception.RecursoNaoEncontradoException;
import com.renan.corebancario.exception.RegraDeNegocioException;
import com.renan.corebancario.exception.SaldoInsuficienteException;
import com.renan.corebancario.model.Conta;
import com.renan.corebancario.model.TipoTransacao;
import com.renan.corebancario.repository.ContaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Orquestra a transferencia de valores entre duas contas.
 *
 * Esta e a operacao mais sensivel do sistema: precisa ser atomica (ou o
 * dinheiro sai da conta de origem e entra na de destino, ou nada acontece)
 * e precisa impedir que o saldo fique negativo mesmo sob concorrencia.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferenciaService {

    private final ContaRepository contaRepository;
    private final ContaService contaService;

    /**
     * @Transactional garante que o debito na origem, o credito no destino e
     * os dois lancamentos no extrato ocorram em uma unica transacao de banco:
     * se qualquer passo falhar (ex.: excecao de saldo insuficiente lancada
     * apos o debito), o Spring aciona o rollback de tudo, e nenhuma das
     * alteracoes e persistida.
     *
     * Propagation.REQUIRED (o padrao) e explicito aqui para deixar claro que
     * esta operacao sempre precisa rodar dentro de uma transacao propria.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public TransferenciaResponseDTO transferir(TransferenciaRequestDTO dto) {
        boolean destinoPorNumero = dto.numeroContaDestino() != null && !dto.numeroContaDestino().isBlank();
        boolean destinoPorChavePix = dto.chavePixDestino() != null && !dto.chavePixDestino().isBlank();
        if (destinoPorNumero == destinoPorChavePix) {
            throw new RegraDeNegocioException("Informe somente o numero da conta ou a chave Pix de destino");
        }

        Conta contaOrigemSemLock = buscarPorNumeroOuFalhar(dto.numeroContaOrigem());
        Conta contaDestinoSemLock = destinoPorChavePix
                ? buscarPorChavePixOuFalhar(dto.chavePixDestino().trim())
                : buscarPorNumeroOuFalhar(dto.numeroContaDestino().trim());

        if (contaOrigemSemLock.getId().equals(contaDestinoSemLock.getId())) {
            throw new RegraDeNegocioException("A conta de origem e a conta de destino nao podem ser a mesma");
        }

        // Para evitar deadlock quando duas transferencias concorrentes
        // envolvem as mesmas duas contas em ordens opostas (A->B e B->A),
        // os locks sao sempre adquiridos na mesma ordem (menor id primeiro).
        Long menorId = Math.min(contaOrigemSemLock.getId(), contaDestinoSemLock.getId());
        Long maiorId = Math.max(contaOrigemSemLock.getId(), contaDestinoSemLock.getId());

        Conta primeiraComLock = obterComLockOuFalhar(menorId);
        Conta segundaComLock = obterComLockOuFalhar(maiorId);

        Conta contaOrigem = contaOrigemSemLock.getId().equals(primeiraComLock.getId())
                ? primeiraComLock : segundaComLock;
        Conta contaDestino = contaOrigemSemLock.getId().equals(primeiraComLock.getId())
                ? segundaComLock : primeiraComLock;

        BigDecimal valor = dto.valor();

        // Validacao estrita de saldo: feita ja com o lock adquirido, para que
        // nenhuma outra transacao possa alterar o saldo entre a checagem e o debito.
        if (!contaOrigem.possuiSaldoSuficiente(valor)) {
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente na conta " + contaOrigem.getNumero()
                            + " para transferir " + valor);
        }

        contaOrigem.debitar(valor);
        contaDestino.creditar(valor);

        contaRepository.save(contaOrigem);
        contaRepository.save(contaDestino);

        String descricao = "Transferencia de " + contaOrigem.getNumero() + " para " + contaDestino.getNumero();
        contaService.registrarLancamento(contaOrigem, TipoTransacao.TRANSFERENCIA_DEBITO, valor,
                contaOrigem, contaDestino, descricao);
        contaService.registrarLancamento(contaDestino, TipoTransacao.TRANSFERENCIA_CREDITO, valor,
                contaOrigem, contaDestino, descricao);

        log.info("Transferencia concluida: {} -> {} no valor de {}",
                contaOrigem.getNumero(), contaDestino.getNumero(), valor);

        return new TransferenciaResponseDTO(
                "Transferencia realizada com sucesso",
                contaOrigem.getNumero(),
                contaDestino.getNumero(),
                valor,
                contaOrigem.getSaldo(),
                LocalDateTime.now()
        );
    }

    private Conta buscarPorNumeroOuFalhar(String numero) {
        return contaRepository.findByNumero(numero)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta nao encontrada com numero: " + numero));
    }

    private Conta buscarPorChavePixOuFalhar(String chavePix) {
        return contaRepository.findByChavePix(chavePix)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma conta encontrada com essa chave Pix"));
    }

    private Conta obterComLockOuFalhar(Long id) {
        return contaRepository.buscarPorIdComLock(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta nao encontrada com id: " + id));
    }
}
