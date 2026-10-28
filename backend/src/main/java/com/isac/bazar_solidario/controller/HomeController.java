package com.isac.bazar_solidario.controller;

import com.isac.bazar_solidario.dto.HomeResponse;
import com.isac.bazar_solidario.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class HomeController {
    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping
    public HomeResponse obter() {
        return homeService.carregar();
    }
}
