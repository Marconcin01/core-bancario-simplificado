package com.renan.corebancario.controller;

import com.renan.corebancario.dto.TransferenciaRequestDTO;
import com.renan.corebancario.dto.TransferenciaResponseDTO;
import com.renan.corebancario.service.TransferenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transferencias")
@RequiredArgsConstructor
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    @PostMapping
    public ResponseEntity<TransferenciaResponseDTO> transferir(@Valid @RequestBody TransferenciaRequestDTO dto) {
        return ResponseEntity.ok(transferenciaService.transferir(dto));
    }
}
