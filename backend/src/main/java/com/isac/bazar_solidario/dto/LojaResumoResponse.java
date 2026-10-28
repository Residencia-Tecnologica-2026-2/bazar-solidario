package com.isac.bazar_solidario.dto;

import com.isac.bazar_solidario.model.Loja;

public record LojaResumoResponse(Long id, String nome, String logoUrl) {

    public static LojaResumoResponse de(Loja loja) {
        return new LojaResumoResponse(loja.getId(), loja.getNome(), loja.getLogoUrl());
    }
}