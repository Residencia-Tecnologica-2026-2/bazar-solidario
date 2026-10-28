package com.isac.bazar_solidario.repository;

import com.isac.bazar_solidario.model.Anuncio;
import com.isac.bazar_solidario.model.enums.StatusAnuncio;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Locale;

/** Filtros dinâmicos da listagem de anúncios. */
public final class AnuncioSpecs {

    private AnuncioSpecs() {
    }

    public static Specification<Anuncio> comStatus(StatusAnuncio status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Anuncio> tituloContem(String titulo) {
        String padrao = "%" + escapar(titulo.toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.<String>get("titulo")), padrao, '\\');
    }

    public static Specification<Anuncio> daCategoria(Long categoriaId) {
        return (root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId);
    }

    public static Specification<Anuncio> precoMinimo(BigDecimal minimo) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<BigDecimal>get("preco"), minimo);
    }

    public static Specification<Anuncio> precoMaximo(BigDecimal maximo) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.<BigDecimal>get("preco"), maximo);
    }

    /** Evita que % e _ digitados pelo usuário virem curingas do LIKE. */
    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
