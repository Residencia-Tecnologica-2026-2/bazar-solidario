package com.isac.bazar_solidario.dto;

import com.isac.bazar_solidario.model.Usuario;

/** Só expõe id e nome: nunca e-mail nem senha. */
public record VendedorResponse(Long id, String nome) {

    public static VendedorResponse de(Usuario usuario) {
        return new VendedorResponse(usuario.getId(), usuario.getNome());
    }
}

