package com.isac.bazar_solidario.dto;

import com.isac.bazar_solidario.model.Categoria;

public record CategoriaResponse(Long id, String nome, String descricao) {

    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getDescricao());
    }
}

