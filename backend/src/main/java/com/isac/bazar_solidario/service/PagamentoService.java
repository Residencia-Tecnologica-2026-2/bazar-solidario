package com.isac.bazar_solidario.service;

import tools.jackson.databind.JsonNode;
import com.isac.bazar_solidario.config.AsaasConfig;
import com.isac.bazar_solidario.dto.PagamentoResponse;
import com.isac.bazar_solidario.model.*;
import com.isac.bazar_solidario.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PagamentoService {
    private final PedidoRepository pedidos;
    private final PagamentoRepository pagamentos;
    private final AsaasConfig config;
    private final AsaasGateway gateway;
    private final TransactionTemplate tx;
    public PagamentoService(PedidoRepository pedidos,PagamentoRepository pagamentos,AsaasConfig config,
            AsaasGateway gateway,PlatformTransactionManager manager) {
        this.pedidos=pedidos;this.pagamentos=pagamentos;this.config=config;this.gateway=gateway;
        this.tx=new TransactionTemplate(manager);
    }
    private void exigir(boolean ok,HttpStatus status,String mensagem){if(!ok)throw new ResponseStatusException(status,mensagem);}
    private Pedido autorizado(Long id,Usuario usuario,boolean apenasCliente) {
        var pedido=pedidos.bloquear(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Pedido não encontrado."));
        boolean cliente=usuario.getRole()==Role.CLIENTE && pedido.getCliente().getId().equals(usuario.getId());
        boolean loja=usuario.getRole()==Role.LOJA && pedido.getVendedor().getId().equals(usuario.getId());
        exigir(cliente || (!apenasCliente && loja),HttpStatus.FORBIDDEN,"Este pedido não pertence à sua conta.");
        return pedido;
    }
    private record Tentativa(Long id,String referencia,String nome,String email,BigDecimal total,BigDecimal comissao,boolean nova){}
    public PagamentoResponse iniciar(Long pedidoId,String cpf,Usuario usuario) {
        exigir(config.configurado(),HttpStatus.CONFLICT,"Sandbox ainda não configurado: falta vincular a loja emissora e a carteira do ISAC.");
        exigir(cpfValido(cpf),HttpStatus.BAD_REQUEST,"Informe um CPF válido do pagador de teste.");
        Tentativa t=tx.execute(status->{
            var pedido=autorizado(pedidoId,usuario,true);
            exigir(pedido.getVendedor().getId()==config.getSellerId(),HttpStatus.CONFLICT,"Este brechó ainda não tem conta de pagamento vinculada ao piloto.");
            exigir(!pedido.getEstado().equals("CANCELADO"),HttpStatus.CONFLICT,"Pedido cancelado.");
            var existente=pagamentos.findByPedidoId(pedidoId);
            if(existente.isPresent()){
                var p=existente.get();return new Tentativa(p.getId(),p.getReferencia(),"","",pedido.getTotal(),pedido.getValorIsac(),false);
            }
            var p=new Pagamento();p.setPedido(pedido);
            p.setAviso("Cobrança em preparação. Se a resposta for inconclusiva, consulte sem criar outra cobrança.");
            pagamentos.saveAndFlush(p);
            return new Tentativa(p.getId(),p.getReferencia(),usuario.getNome(),usuario.getEmail(),pedido.getTotal(),pedido.getValorIsac(),true);
        });
        // O marcador foi confirmado no banco ANTES da operação externa. Falhas nunca disparam novo POST automático.
        if(t.nova()) {
            try{aplicar(t.id(),gateway.cobrar(t.referencia(),t.nome(),t.email(),cpf.replaceAll("\\D",""),t.total(),t.comissao()));}
            catch(IllegalStateException e){tx.executeWithoutResult(s->{var p=pagamentos.bloquear(t.id()).orElseThrow();p.setAviso(e.getMessage());});}
        }
        return resposta(t.id());
    }
    public PagamentoResponse consultar(Long pedidoId,Usuario usuario) {
        Long id=tx.execute(s->{autorizado(pedidoId,usuario,false);return pagamentos.findByPedidoId(pedidoId).map(Pagamento::getId)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Nenhum pagamento iniciado."));});
        reconciliar(id);
        return resposta(id);
    }
    public void webhook(String provedorId) {
        exigir(config.configurado(),HttpStatus.SERVICE_UNAVAILABLE,"Integração indisponível.");
        if(provedorId==null || !provedorId.matches("pay_[A-Za-z0-9]+")) return;
        var pagamento=pagamentos.findByProvedorId(provedorId);
        // Em timeout de criação, a reconciliação pelo pedido recupera o ID via referência única.
        if(pagamento.isPresent()) reconciliar(pagamento.get().getId());
    }
    private void reconciliar(Long id) {
        var dados=tx.execute(s->{var p=pagamentos.findById(id).orElseThrow();return new String[]{p.getProvedorId(),p.getReferencia()};});
        JsonNode remoto;
        if(dados[0]!=null) remoto=gateway.consultar("/payments/"+dados[0]);
        else {
            var encontrados=gateway.consultar("/payments?externalReference="+dados[1]);
            if(encontrados.path("data").size()==0)return;
            exigir(encontrados.path("data").size()==1,HttpStatus.CONFLICT,"Mais de uma cobrança encontrada. É necessária conciliação pela equipe.");
            remoto=encontrados.path("data").get(0);
        }
        aplicar(id,remoto);
    }
    private void aplicar(Long id,JsonNode remoto) {
        tx.executeWithoutResult(s->{
            var p=pagamentos.bloquear(id).orElseThrow();
            exigir(p.getReferencia().equals(remoto.path("externalReference").asText()),HttpStatus.CONFLICT,"Referência do pagamento divergente.");
            BigDecimal valor;
            try{valor=new BigDecimal(remoto.path("value").asText());}
            catch(NumberFormatException e){throw new ResponseStatusException(HttpStatus.CONFLICT,"Valor do pagamento ausente.");}
            exigir(valor.compareTo(p.getPedido().getTotal())==0,HttpStatus.CONFLICT,"Valor do pagamento divergente.");
            String provedorId=remoto.path("id").asText();
            exigir(provedorId.matches("pay_[A-Za-z0-9]+"),HttpStatus.CONFLICT,"Identificador de cobrança inválido.");
            exigir(p.getProvedorId()==null || p.getProvedorId().equals(provedorId),HttpStatus.CONFLICT,"Cobrança diferente da registrada.");
            p.setProvedorId(provedorId);
            String url=remoto.path("invoiceUrl").asText();
            if(!url.isBlank()) {
                URI uri=URI.create(url);
                exigir("https".equals(uri.getScheme()) && "sandbox.asaas.com".equals(uri.getHost()) && uri.getUserInfo()==null,
                    HttpStatus.CONFLICT,"Endereço de checkout inesperado.");
                p.setCheckoutUrl(url);
            }
            String situacao=remoto.path("status").asText();
            // Uma consulta iniciada antes de outra não pode regredir recebimento/estorno já persistido.
            if("ESTORNADO".equals(p.getEstado()) && !"REFUNDED".equals(situacao))return;
            if("PAGO".equals(p.getEstado()) && Set.of("PENDING","OVERDUE","CONFIRMED").contains(situacao))return;
            // Consulta autoritativa: callbacks do navegador ou payload de webhook não determinam o pagamento.
            p.setEstado(switch(situacao){
                case "RECEIVED" -> "PAGO";
                case "CONFIRMED" -> "CONFIRMADO_PROVEDOR";
                case "REFUNDED" -> "ESTORNADO";
                case "REFUND_REQUESTED", "REFUND_IN_PROGRESS" -> "ESTORNO_EM_PROCESSAMENTO";
                case "CHARGEBACK_REQUESTED", "CHARGEBACK_DISPUTE", "AWAITING_CHARGEBACK_REVERSAL" -> "CONTESTADO";
                case "OVERDUE" -> "VENCIDO";
                case "PENDING" -> "AGUARDANDO_PAGAMENTO";
                default -> "EM_ANALISE";
            });
            if(remoto.path("deleted").asBoolean(false))p.setEstado("EXCLUIDO_PROVEDOR");
            if(remoto.hasNonNull("netValue")){
                var liquido=new BigDecimal(remoto.path("netValue").asText());
                p.setLiquido(liquido);p.setTarifa(valor.subtract(liquido));
                p.setLiquidoBrecho(liquido.subtract(p.getPedido().getValorIsac()));
            }
            p.setAviso("Ambiente de testes. A comissão foi solicitada ao provedor; o recebimento do pagamento não comprova a liquidação do split.");
            p.setAtualizadoEm(LocalDateTime.now());
        });
    }
    private PagamentoResponse resposta(Long id){return tx.execute(s->PagamentoResponse.de(pagamentos.findById(id).orElseThrow()));}
    static boolean cpfValido(String entrada) {
        if(entrada==null)return false;
        var cpf=entrada.replaceAll("\\D","");
        if(cpf.length()!=11 || cpf.chars().distinct().count()==1)return false;
        for(int pos=9;pos<11;pos++){
            int soma=0;for(int i=0;i<pos;i++)soma+=(cpf.charAt(i)-'0')*(pos+1-i);
            int digito=11-soma%11;if(digito>=10)digito=0;
            if(digito!=cpf.charAt(pos)-'0')return false;
        }
        return true;
    }
}
