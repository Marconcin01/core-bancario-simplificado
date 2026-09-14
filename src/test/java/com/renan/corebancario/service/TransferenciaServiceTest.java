package com.renan.corebancario.service;

import com.renan.corebancario.dto.TransferenciaRequestDTO;
import com.renan.corebancario.exception.RegraDeNegocioException;
import com.renan.corebancario.exception.SaldoInsuficienteException;
import com.renan.corebancario.model.Cliente;
import com.renan.corebancario.model.Conta;
import com.renan.corebancario.repository.ContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios da regra mais critica do sistema: a transferencia entre contas.
 * O ContaRepository e mockado para isolar a logica de negocio da camada de persistencia.
 */
@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private ContaService contaService;

    @InjectMocks
    private TransferenciaService transferenciaService;

    private Conta contaOrigem;
    private Conta contaDestino;

    @BeforeEach
    void setUp() {
        Cliente cliente = Cliente.builder().id(1L).nome("Fulano").cpf("12345678900").email("fulano@teste.com").build();
        Cliente outroCliente = Cliente.builder().id(2L).nome("Ciclano").cpf("98765432100").email("ciclano@teste.com").build();

        contaOrigem = Conta.builder().id(10L).numero("11111111").saldo(new BigDecimal("100.00")).cliente(cliente).build();
        contaDestino = Conta.builder().id(20L).numero("22222222").saldo(new BigDecimal("50.00")).cliente(outroCliente).build();
    }

    @Test
    void deveTransferirComSucessoQuandoSaldoSuficiente() {
        when(contaRepository.findByNumero("11111111")).thenReturn(Optional.of(contaOrigem));
        when(contaRepository.findByNumero("22222222")).thenReturn(Optional.of(contaDestino));
        when(contaRepository.buscarPorIdComLock(10L)).thenReturn(Optional.of(contaOrigem));
        when(contaRepository.buscarPorIdComLock(20L)).thenReturn(Optional.of(contaDestino));
        when(contaRepository.save(any(Conta.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        var resposta = transferenciaService.transferir(
                new TransferenciaRequestDTO("11111111", "22222222", new BigDecimal("30.00")));

        assertThat(resposta.saldoAtualContaOrigem()).isEqualTo(new BigDecimal("70.00"));
        assertThat(contaDestino.getSaldo()).isEqualTo(new BigDecimal("80.00"));
    }

    @Test
    void deveLancarExcecaoQuandoSaldoInsuficiente() {
        when(contaRepository.findByNumero("11111111")).thenReturn(Optional.of(contaOrigem));
        when(contaRepository.findByNumero("22222222")).thenReturn(Optional.of(contaDestino));
        when(contaRepository.buscarPorIdComLock(10L)).thenReturn(Optional.of(contaOrigem));
        when(contaRepository.buscarPorIdComLock(20L)).thenReturn(Optional.of(contaDestino));

        assertThatThrownBy(() -> transferenciaService.transferir(
                new TransferenciaRequestDTO("11111111", "22222222", new BigDecimal("1000.00"))))
                .isInstanceOf(SaldoInsuficienteException.class);

        // Garante que, com saldo insuficiente, nenhum saldo foi alterado.
        assertThat(contaOrigem.getSaldo()).isEqualTo(new BigDecimal("100.00"));
        assertThat(contaDestino.getSaldo()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    void deveLancarExcecaoQuandoContaOrigemIgualContaDestino() {
        assertThatThrownBy(() -> transferenciaService.transferir(
                new TransferenciaRequestDTO("11111111", "11111111", new BigDecimal("10.00"))))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}
