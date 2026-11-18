package com.isac.bazar_solidario.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name="pagamentos") @Getter @Setter @NoArgsConstructor
public class Pagamento {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(optional=false) @JoinColumn(name="pedido_id",unique=true) private Pedido pedido;
    @Column(nullable=false,unique=true) private String referencia=UUID.randomUUID().toString();
    @Column(unique=true) private String provedorId;
    @Column(nullable=false) private String estado="RECONCILIAR";
    private String checkoutUrl;
    @Column(length=250) private String aviso;
    @Column(precision=16,scale=2) private BigDecimal liquido;
    @Column(precision=16,scale=2) private BigDecimal tarifa;
    @Column(precision=16,scale=2) private BigDecimal liquidoBrecho;
    private LocalDateTime atualizadoEm=LocalDateTime.now();
}
