package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.dto.LoginRequestDTO;
import com.isac.bazar_solidario.dto.LoginResponseDTO;
import com.isac.bazar_solidario.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthService(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO dados) {
        Usuario usuario = usuarioService.validarCredenciais(
                dados.email(),
                dados.senha()
        );

        String token = jwtService.generateToken(usuario.getEmail());

        return new LoginResponseDTO(token);
    }
}
