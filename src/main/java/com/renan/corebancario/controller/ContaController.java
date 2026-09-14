package com.renan.corebancario.controller;

import com.renan.corebancario.dto.*;
import com.renan.corebancario.service.ContaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/contas")
@RequiredArgsConstructor
public class ContaController {

    private final ContaService contaService;

    @PostMapping
    public ResponseEntity<ContaResponseDTO> abrirConta(@Valid @RequestBody ContaRequestDTO dto) {
        ContaResponseDTO contaCriada = contaService.abrirConta(dto);
        return ResponseEntity
                .created(URI.create("/api/contas/" + contaCriada.id()))
                .body(contaCriada);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<ContaResponseDTO>> listarPorCliente(@RequestParam Long clienteId) {
        return ResponseEntity.ok(contaService.listarPorCliente(clienteId));
    }

    @GetMapping("/{numero}/saldo")
    public ResponseEntity<SaldoResponseDTO> consultarSaldo(@PathVariable String numero) {
        return ResponseEntity.ok(contaService.consultarSaldo(numero));
    }

    @GetMapping("/{numero}/extrato")
    public ResponseEntity<ExtratoResponseDTO> consultarExtrato(
            @PathVariable String numero,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanhoPagina) {
        return ResponseEntity.ok(contaService.consultarExtrato(numero, pagina, tamanhoPagina));
    }
}
