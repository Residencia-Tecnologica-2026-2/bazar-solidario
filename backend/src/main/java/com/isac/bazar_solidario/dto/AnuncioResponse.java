package com.isac.bazar_solidario.dto;

import com.isac.bazar_solidario.model.Anuncio;
import com.isac.bazar_solidario.model.Imagem;
import com.isac.bazar_solidario.model.enums.StatusAnuncio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AnuncioResponse(
        Long id,
        String titulo,
        String descricao,
        BigDecimal preco,
        Integer estoque,
        StatusAnuncio status,
        LocalDateTime dataCriacao,
        CategoriaResponse categoria,
        VendedorResponse vendedor,
        List<String> imagens
) {

    public static AnuncioResponse de(Anuncio a) {
        return new AnuncioResponse(
                a.getId(),
                a.getTitulo(),
                a.getDescricao(),
                a.getPreco(),
                a.getEstoque(),
                a.getStatus(),
                a.getDataCriacao(),
                CategoriaResponse.de(a.getCategoria()),
                VendedorResponse.de(a.getVendedor()),
                a.getImagens().stream().map(Imagem::getUrl).toList());
    }
}
