package com.isac.bazar_solidario.repository;
import com.isac.bazar_solidario.model.Pedido;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.domain.*;
public interface PedidoRepository extends JpaRepository<Pedido,Long> {
 Optional<Pedido> findByClienteIdAndChave(Long clienteId,String chave);
 Page<Pedido> findByClienteIdOrderByIdDesc(Long id,Pageable page);
 Page<Pedido> findByVendedorIdOrderByIdDesc(Long id,Pageable page);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Pedido p where p.id=:id") Optional<Pedido> bloquear(@Param("id") Long id);
}
