package com.isac.bazar_solidario.repository;
import com.isac.bazar_solidario.model.Pagamento;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface PagamentoRepository extends JpaRepository<Pagamento,Long> {
    Optional<Pagamento> findByPedidoId(Long pedidoId);
    Optional<Pagamento> findByProvedorId(String provedorId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pagamento p where p.id=:id")
    Optional<Pagamento> bloquear(@Param("id") Long id);
}
