package com.isac.bazar_solidario.dto;

import com.isac.bazar_solidario.model.Usuario;

/** Dados do usuário que podem sair na API (nunca a senha). */
public record UsuarioResponse(Long id, String nome, String email, String role) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getRole().name());
    }
}
