package com.renan.corebancario.service;

import com.renan.corebancario.dto.ClienteRequestDTO;
import com.renan.corebancario.dto.ClienteResponseDTO;
import com.renan.corebancario.exception.RecursoNaoEncontradoException;
import com.renan.corebancario.exception.RegraDeNegocioException;
import com.renan.corebancario.model.Cliente;
import com.renan.corebancario.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negocio relacionadas ao cadastro de clientes.
 *
 * Responsabilidade unica (SRP): esta classe cuida apenas do ciclo de vida
 * do Cliente. Abertura de conta e transferencias ficam em servicos proprios.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteResponseDTO cadastrar(ClienteRequestDTO dto) {
        if (clienteRepository.existsByCpf(dto.cpf())) {
            throw new RegraDeNegocioException("Ja existe um cliente cadastrado com o CPF informado");
        }
        if (clienteRepository.existsByEmail(dto.email())) {
            throw new RegraDeNegocioException("Ja existe um cliente cadastrado com o email informado");
        }

        Cliente cliente = Cliente.builder()
                .nome(dto.nome())
                .cpf(dto.cpf())
                .email(dto.email())
                .build();

        Cliente salvo = clienteRepository.save(cliente);
        return ClienteResponseDTO.from(salvo);
    }

    public ClienteResponseDTO buscarPorId(Long id) {
        return ClienteResponseDTO.from(buscarClienteOuFalhar(id));
    }

    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(ClienteResponseDTO::from)
                .toList();
    }

    /**
     * Busca a entidade Cliente ou lanca exececao de nao encontrado.
     * Metodo de uso interno, reaproveitado por outros servicos (ex.: ContaService)
     * que precisam da entidade completa e nao apenas do DTO de resposta.
     */
    protected Cliente buscarClienteOuFalhar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado com id: " + id));
    }
}
