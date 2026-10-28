package com.isac.bazar_solidario.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="itens_pedido") @Getter @Setter @NoArgsConstructor
public class ItemPedido {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) private Pedido pedido;
 @ManyToOne(optional=false) private Anuncio anuncio;
 @Column(nullable=false,length=150) private String titulo;
 @Column(nullable=false) private Integer quantidade;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal precoUnitario;
}
