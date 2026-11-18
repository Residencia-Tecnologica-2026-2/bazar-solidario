package com.isac.bazar_solidario.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity @Getter @Setter
@Table(name="mercado_livre_vinculos")
public class MercadoLivreVinculo {
    @Id private Long anuncioId;
    @Column(nullable=false) private Long usuarioId;
    @Column(nullable=false, unique=true, length=30) private String itemId;
    @Column(nullable=false, length=30) private String statusExterno;
    @Column(nullable=false) private int estoqueExterno;
    @Column(nullable=false) private Instant consultadoEm;
}
