package com.isac.bazar_solidario.dto;
import com.isac.bazar_solidario.model.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public record PedidoResponse(Long id,String estado,Long vendedorId,String brecho,BigDecimal total,BigDecimal valorIsac,BigDecimal valorBrecho,LocalDateTime criadoEm,List<Item> itens) {
 public record Item(Long anuncioId,String titulo,int quantidade,BigDecimal precoUnitario) {}
 public static PedidoResponse de(Pedido p) {return new PedidoResponse(p.getId(),p.getEstado(),p.getVendedor().getId(),p.getVendedor().getNome(),p.getTotal(),p.getValorIsac(),p.getValorBrecho(),p.getCriadoEm(),p.getItens().stream().map(i->new Item(i.getAnuncio().getId(),i.getTitulo(),i.getQuantidade(),i.getPrecoUnitario())).toList());}
}
