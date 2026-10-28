package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.model.Loja;
import com.isac.bazar_solidario.repository.LojaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class LojaAutenticadaService {

    private final LojaRepository lojaRepository;

    public LojaAutenticadaService(LojaRepository lojaRepository) {
        this.lojaRepository = lojaRepository;
    }

    public Loja obter(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        return lojaRepository.findByUsuarioEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Loja do usuário não encontrada"));
    }

    public Optional<Loja> obterOpcional(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        return lojaRepository.findByUsuarioEmail(authentication.getName());
    }
}