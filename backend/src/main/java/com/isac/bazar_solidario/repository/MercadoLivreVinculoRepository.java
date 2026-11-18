package com.isac.bazar_solidario.repository;
import com.isac.bazar_solidario.model.MercadoLivreVinculo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MercadoLivreVinculoRepository extends JpaRepository<MercadoLivreVinculo,Long> {
    List<MercadoLivreVinculo> findByUsuarioIdOrderByAnuncioId(Long usuarioId);
}
