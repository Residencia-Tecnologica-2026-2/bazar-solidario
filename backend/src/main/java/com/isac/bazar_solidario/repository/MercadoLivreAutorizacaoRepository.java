package com.isac.bazar_solidario.repository;
import com.isac.bazar_solidario.model.MercadoLivreAutorizacao;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;

public interface MercadoLivreAutorizacaoRepository extends JpaRepository<MercadoLivreAutorizacao,String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from MercadoLivreAutorizacao a where a.hash=:hash")
    Optional<MercadoLivreAutorizacao> bloquear(@org.springframework.data.repository.query.Param("hash") String hash);
    void deleteByExpiraEmBefore(Instant instant);
}
