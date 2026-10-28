package com.isac.bazar_solidario.repository;

import com.isac.bazar_solidario.model.Encontro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository 

public interface EncontroRepository extends JpaRepository<Encontro, Long> {
    @Query("SELECT e FROM Encontro e WHERE e.status = 'ATIVO' AND e.data >= :hoje ORDER BY e.data ASC, e.horario ASC")
    List<Encontro> listarProximosAtivos(@Param("hoje") LocalDate hoje);
}

