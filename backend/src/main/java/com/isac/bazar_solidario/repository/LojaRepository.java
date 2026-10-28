package com.isac.bazar_solidario.repository;

import com.isac.bazar_solidario.model.Loja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LojaRepository extends JpaRepository<Loja, Long> {
    Optional<Loja> findByUsuarioId(Long usuarioId);
    Optional<Loja> findByUsuarioEmail(String email);
}
