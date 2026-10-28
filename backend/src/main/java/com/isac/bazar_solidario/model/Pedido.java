package com.isac.bazar_solidario.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
@Entity @Table(name="pedidos", uniqueConstraints=@UniqueConstraint(columnNames={"cliente_id","chave"}))
@Getter @Setter @NoArgsConstructor
public class Pedido {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) private Usuario cliente;
 @ManyToOne(optional=false) private Usuario vendedor;
 @Column(nullable=false,length=64) private String chave;
 @Column(nullable=false,length=1000) private String requisicao;
 @Column(nullable=false) private String estado="AGUARDANDO_CONFIRMACAO_MANUAL";
 @Column(nullable=false,precision=16,scale=2) private BigDecimal total;
 @Column(nullable=false,precision=16,scale=2) private BigDecimal valorIsac;
 @Column(nullable=false,precision=16,scale=2) private BigDecimal valorBrecho;
 @Column(nullable=false) private LocalDateTime criadoEm=LocalDateTime.now();
 @OneToMany(mappedBy="pedido",cascade=CascadeType.ALL,orphanRemoval=true) private List<ItemPedido> itens=new ArrayList<>();
}
