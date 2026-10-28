package com.isac.bazar_solidario.dto;

import com.isac.bazar_solidario.model.Loja;

public record LojaResponse(
        Long id,
        String nome,
        String logoUrl,
        String descricao,
        String endereco,
        Double latitude,
        Double longitude,
        String whatsapp,
        Long vendedorId
) {
    public static LojaResponse de(Loja loja) {
        return new LojaResponse(
                loja.getId(),
                loja.getNome(),
                loja.getLogoUrl(),
                loja.getDescricao(),
                loja.getEndereco(),
                loja.getLatitude(),
                loja.getLongitude(),
                loja.getWhatsapp(), loja.getUsuario().getId());
    }
}