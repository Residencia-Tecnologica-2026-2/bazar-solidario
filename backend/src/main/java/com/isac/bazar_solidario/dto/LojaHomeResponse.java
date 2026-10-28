package com.isac.bazar_solidario.dto;

import com.isac.bazar_solidario.model.Loja;

public record LojaHomeResponse(
        Long id,
        String nome,
        String logoUrl,
        String endereco,
        Double latitude,
        Double longitude
) {
    public static LojaHomeResponse de(Loja loja) {
        return new LojaHomeResponse(
                loja.getId(),
                loja.getNome(),
                loja.getLogoUrl(),
                loja.getEndereco(),
                loja.getLatitude(),
                loja.getLongitude());
    }
}
