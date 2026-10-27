package com.isac.bazar_solidario.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.isac.bazar_solidario.repository.LojaRepository;
import com.isac.bazar_solidario.model.Loja;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service 

public class LojaAutenticadaService {
    private final LojaRepository lojaRepository;

    public LojaAutenticadaService(LojaRepository lojaRepository) {
        this.lojaRepository = lojaRepository;
    }
    public Loja obter(Authentication authentication) {
        String email = authentication.getName();        
        return lojaRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loja não encontrada para o usuário autenticado"));
    }
}

