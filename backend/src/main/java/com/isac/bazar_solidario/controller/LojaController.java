package com.isac.bazar_solidario.controller;

import com.isac.bazar_solidario.dto.LojaAtualizacaoRequest;
import com.isac.bazar_solidario.dto.LojaResponse;
import com.isac.bazar_solidario.dto.PageResponse;
import com.isac.bazar_solidario.service.LojaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lojas")
@RequiredArgsConstructor
public class LojaController {

    private final LojaService lojaService;

    @GetMapping
    public PageResponse<LojaResponse> listar(Pageable pageable) {
        return lojaService.listar(pageable);
    }

    @GetMapping("/{id}")
    public LojaResponse obter(@PathVariable Long id) {
        return lojaService.obter(id);
    }

    @GetMapping("/me")
    public LojaResponse minhaLoja(Authentication authentication) {
        return lojaService.minhaLoja(authentication);
    }

    @PutMapping("/me")
    public LojaResponse atualizar(Authentication authentication,
                                 @Valid @RequestBody LojaAtualizacaoRequest request) {
        return lojaService.atualizar(authentication, request);
    }
}