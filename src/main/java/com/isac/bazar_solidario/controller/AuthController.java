package com.isac.bazar_solidario.controller;

import com.isac.bazar_solidario.dto.LoginRequestDTO;
import com.isac.bazar_solidario.dto.LoginResponseDTO;
import com.isac.bazar_solidario.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO dados) {
        return authService.login(dados);
    }
}