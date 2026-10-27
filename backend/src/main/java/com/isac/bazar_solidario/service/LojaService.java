package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.dto.LojaAtualizacaoRequest;
import com.isac.bazar_solidario.dto.LojaResponse;
import com.isac.bazar_solidario.dto.PageResponse;
import com.isac.bazar_solidario.model.Loja;
import com.isac.bazar_solidario.repository.LojaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LojaService {

    private final LojaRepository lojaRepository;
    private final LojaAutenticadaService lojaAutenticadaService;

    @Transactional(readOnly = true)
    public PageResponse<LojaResponse> listar(Pageable pageable) {
        return PageResponse.de(lojaRepository.findAll(pageable).map(LojaResponse::de));
    }

    @Transactional(readOnly = true)
    public LojaResponse obter(Long id) {
        return lojaRepository.findById(id)
                .map(LojaResponse::de)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Loja não encontrada"));
    }

    @Transactional(readOnly = true)
    public LojaResponse minhaLoja(Authentication authentication) {
        return LojaResponse.de(lojaAutenticadaService.obter(authentication));
    }

    @Transactional
    public LojaResponse atualizar(Authentication authentication, LojaAtualizacaoRequest request) {
        Loja loja = lojaAutenticadaService.obter(authentication);
        loja.setNome(request.nome().trim());
        loja.setLogoUrl(normalizar(request.logoUrl()));
        loja.setDescricao(normalizar(request.descricao()));
        loja.setEndereco(normalizar(request.endereco()));
        loja.setLatitude(request.latitude());
        loja.setLongitude(request.longitude());
        loja.setWhatsapp(request.whatsapp());

        return LojaResponse.de(lojaRepository.save(loja));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}