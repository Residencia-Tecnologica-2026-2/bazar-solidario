package com.isac.bazar_solidario.dto;

import com.isac.bazar_solidario.model.Anuncio;
import com.isac.bazar_solidario.model.Imagem;
import com.isac.bazar_solidario.model.enums.StatusAnuncio;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        LojaResumoResponse loja,
        List<String> imagens,
        String whatsappLink
) {

    public static AnuncioResponse de(Anuncio a) {
        return new AnuncioResponse(
                a.getId(), a.getTitulo(), a.getDescricao(), a.getPreco(),
                a.getEstoque(), a.getStatus(), a.getDataCriacao(),
                CategoriaResponse.de(a.getCategoria()),
                LojaResumoResponse.de(a.getLoja()),
                a.getImagens().stream().map(Imagem::getUrl).toList(),
                montarWhatsappLink(a));
    }

    private static String montarWhatsappLink(Anuncio a) {
        if (a.getLoja() == null || a.getLoja().getWhatsapp() == null || a.getLoja().getWhatsapp().isBlank()) {
            return null;
        }
        String mensagem = String.format(
                "Olá! Tenho interesse na peça \"%s\" (R$ %.2f) da loja %s.",
                a.getTitulo(), a.getPreco(), a.getLoja().getNome());
        String encoded = URLEncoder.encode(mensagem, StandardCharsets.UTF_8);
        return "https://wa.me/" + a.getLoja().getWhatsapp() + "?text=" + encoded;
    }
}
