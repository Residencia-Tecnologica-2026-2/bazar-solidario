package com.isac.bazar_solidario.repository;

import com.isac.bazar_solidario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Consulta de Usuario usada só pelo módulo de anúncios.
 * Existe para não alterar o UsuarioRepository (de outro módulo).
 * Se o time adicionar findByEmail lá, esta interface pode ser removida.
 */
public interface VendedorRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
}

