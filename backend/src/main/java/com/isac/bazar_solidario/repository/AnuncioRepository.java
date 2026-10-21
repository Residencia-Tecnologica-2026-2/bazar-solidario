package com.isac.bazar_solidario.repository;

import com.isac.bazar_solidario.model.Anuncio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AnuncioRepository
        extends JpaRepository<Anuncio, Long>, JpaSpecificationExecutor<Anuncio> {

    /** Listagem filtrada; carrega categoria e vendedor junto para evitar N+1. */
    @Override
    @EntityGraph(attributePaths = {"categoria", "vendedor"})
    Page<Anuncio> findAll(Specification<Anuncio> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"categoria", "vendedor"})
    Page<Anuncio> findByVendedorId(Long vendedorId, Pageable pageable);
}

