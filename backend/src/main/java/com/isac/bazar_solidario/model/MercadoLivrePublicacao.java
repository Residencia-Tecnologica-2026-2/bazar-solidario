package com.isac.bazar_solidario.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity @Getter @Setter @Table(name="mercado_livre_publicacoes")
public class MercadoLivrePublicacao {
    @Id private Long anuncioId;
    @Column(nullable=false) private Long usuarioId;
    @Column(nullable=false,length=30) private String estado;
    @Column(length=30) private String itemId;
    @Column(nullable=false,length=250) private String aviso;
    @Column(nullable=false) private Instant atualizadoEm;
}
