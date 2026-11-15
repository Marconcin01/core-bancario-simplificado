package com.renan.corebancario.controller;

import com.renan.corebancario.dto.AtualizarStatusCartaoDTO;
import com.renan.corebancario.dto.CartaoResponseDTO;
import com.renan.corebancario.service.CartaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CartaoController {

    private final CartaoService cartaoService;

    @GetMapping("/contas/{numeroConta}/cartoes")
    public ResponseEntity<List<CartaoResponseDTO>> listarPorConta(@PathVariable String numeroConta) {
        return ResponseEntity.ok(cartaoService.listarPorConta(numeroConta));
    }

    @PostMapping("/contas/{numeroConta}/cartoes")
    public ResponseEntity<CartaoResponseDTO> criar(@PathVariable String numeroConta) {
        CartaoResponseDTO cartao = cartaoService.criar(numeroConta);
        return ResponseEntity.created(URI.create("/api/cartoes/" + cartao.id())).body(cartao);
    }

    @PatchMapping("/cartoes/{cartaoId}/status")
    public ResponseEntity<CartaoResponseDTO> atualizarStatus(
            @PathVariable Long cartaoId,
            @Valid @RequestBody AtualizarStatusCartaoDTO dto) {
        return ResponseEntity.ok(cartaoService.atualizarStatus(cartaoId, dto.status()));
    }
}
