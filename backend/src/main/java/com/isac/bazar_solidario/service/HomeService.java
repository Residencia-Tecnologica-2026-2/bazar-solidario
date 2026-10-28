package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.dto.HomeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeService {
    private static final int ENCONTROS_NA_HOME = 5;
    private static final int LOJAS_NA_HOME = 12;

    private final EncontroService encontroService;
    private final LojaService lojaService;

    public HomeService(EncontroService encontroService, LojaService lojaService) {
        this.encontroService = encontroService;
        this.lojaService = lojaService;
    }

    @Transactional(readOnly = true)
    public HomeResponse carregar() {
        return new HomeResponse(
                encontroService.listarProximos(ENCONTROS_NA_HOME),
                lojaService.listarParaHome(LOJAS_NA_HOME));
    }
}
