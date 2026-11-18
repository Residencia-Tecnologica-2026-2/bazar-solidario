package com.isac.bazar_solidario.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity @Getter @Setter
@Table(name="mercado_livre_autorizacoes")
public class MercadoLivreAutorizacao {
    @Id @Column(length=64) private String hash;
    @Column(nullable=false) private Long usuarioId;
    @Column(nullable=false, length=512) private String verificadorCifrado;
    @Column(nullable=false) private Instant expiraEm;
}
