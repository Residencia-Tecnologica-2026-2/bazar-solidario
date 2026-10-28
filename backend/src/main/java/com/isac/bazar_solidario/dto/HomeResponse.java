package com.isac.bazar_solidario.dto;

import java.util.List;

public record HomeResponse(
        List<EncontroResponse> encontros,
        List<LojaHomeResponse> lojas
) {
    public HomeResponse {
        encontros = List.copyOf(encontros);
        lojas = List.copyOf(lojas);
    }
}
