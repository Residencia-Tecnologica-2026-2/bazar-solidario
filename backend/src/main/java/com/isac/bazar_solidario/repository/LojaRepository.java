package com.isac.bazar_solidario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.isac.bazar_solidario.model.Loja;
import java.util.Optional;

@Repository 
public interface LojaRepository extends JpaRepository<Loja, Long> {
    Optional<Loja> findByUsuarioEmail(String email);
}

