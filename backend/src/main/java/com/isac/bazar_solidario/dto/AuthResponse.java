package com.isac.bazar_solidario.dto;

public record AuthResponse(String token, String tipo, long expiraEmMs, UsuarioResponse usuario) {

    public static AuthResponse de(String token, long expiraEmMs, UsuarioResponse usuario) {
        return new AuthResponse(token, "Bearer", expiraEmMs, usuario);
    }
}
