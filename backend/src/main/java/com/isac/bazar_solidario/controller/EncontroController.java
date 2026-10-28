package com.isac.bazar_solidario.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.isac.bazar_solidario.dto.EncontroRequest;
import com.isac.bazar_solidario.dto.EncontroResponse;
import com.isac.bazar_solidario.service.EncontroService;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/encontros")

public class EncontroController {
    private final EncontroService encontroService;

    public EncontroController(EncontroService encontroService) {
        this.encontroService = encontroService;

    }
    @GetMapping
    public ResponseEntity<List<EncontroResponse>> listar() {
        return ResponseEntity.ok(encontroService.listarProximosAtivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EncontroResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(encontroService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<EncontroResponse> criar(@Valid @RequestBody EncontroRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(encontroService.criar(request, authentication));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EncontroResponse> atualizar(@PathVariable Long id,
                                                      @Valid @RequestBody EncontroRequest request,
                                                      Authentication authentication) {
        return ResponseEntity.ok(encontroService.atualizar(id, request, authentication));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, Authentication authentication) {
        encontroService.cancelar(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
