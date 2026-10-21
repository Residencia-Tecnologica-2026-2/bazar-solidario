package com.isac.bazar_solidario.controller;

import com.isac.bazar_solidario.dto.AuthResponse;
import com.isac.bazar_solidario.dto.LoginRequest;
import com.isac.bazar_solidario.dto.UsuarioResponse;
import com.isac.bazar_solidario.service.UsuarioAutenticadoService;
import com.isac.bazar_solidario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final UsuarioAutenticadoService autenticadoService;

    public AuthController(UsuarioService usuarioService, UsuarioAutenticadoService autenticadoService) {
        this.usuarioService = usuarioService;
        this.autenticadoService = autenticadoService;
    }

    /** Login: e-mail + senha -> token JWT. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return usuarioService.login(request);
    }

    /** Dados de quem está logado (útil para o frontend testar o token). */
    @GetMapping("/me")
    public UsuarioResponse me(Authentication authentication) {
        return UsuarioResponse.de(autenticadoService.obter(authentication));
    }
}
