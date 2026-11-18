package com.isac.bazar_solidario.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity @Getter @Setter
@Table(name="mercado_livre_contas")
public class MercadoLivreConta {
    @Id private Long usuarioId;
    @Column(nullable=false, unique=true) private Long vendedorExterno;
    @Column(nullable=false, length=200) private String apelido;
    @Column(nullable=false, length=4096) private String accessTokenCifrado;
    @Column(nullable=false, length=4096) private String refreshTokenCifrado;
    @Column(nullable=false) private Instant expiraEm;
}
