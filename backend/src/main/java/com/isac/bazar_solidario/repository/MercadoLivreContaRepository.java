package com.isac.bazar_solidario.repository;
import com.isac.bazar_solidario.model.MercadoLivreConta;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface MercadoLivreContaRepository extends JpaRepository<MercadoLivreConta,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from MercadoLivreConta c where c.usuarioId=:id")
    Optional<MercadoLivreConta> bloquear(@org.springframework.data.repository.query.Param("id") Long id);
}
