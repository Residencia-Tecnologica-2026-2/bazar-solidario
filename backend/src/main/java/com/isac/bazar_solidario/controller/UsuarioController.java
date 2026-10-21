package com.isac.bazar_solidario.controller;

import com.isac.bazar_solidario.dto.AuthResponse;
import com.isac.bazar_solidario.dto.RegistroRequest;
import com.isac.bazar_solidario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Cadastro: cria o usuário e devolve o token JWT. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse cadastrar(@Valid @RequestBody RegistroRequest request) {
        return usuarioService.cadastrar(request);
    }
}
