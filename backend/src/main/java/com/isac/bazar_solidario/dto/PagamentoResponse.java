package com.isac.bazar_solidario.dto;
import com.isac.bazar_solidario.model.Pagamento;
import java.math.BigDecimal;
public record PagamentoResponse(Long pedidoId,String estado,String checkoutUrl,String aviso,
    BigDecimal total,BigDecimal comissaoIsac,BigDecimal tarifa,BigDecimal liquidoBrecho,String ambiente) {
    public static PagamentoResponse de(Pagamento p) {
        return new PagamentoResponse(p.getPedido().getId(),p.getEstado(),p.getCheckoutUrl(),p.getAviso(),
            p.getPedido().getTotal(),p.getPedido().getValorIsac(),p.getTarifa(),p.getLiquidoBrecho(),"SANDBOX");
    }
}
