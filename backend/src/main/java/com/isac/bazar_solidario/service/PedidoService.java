package com.isac.bazar_solidario.service;
import com.isac.bazar_solidario.model.*;
import com.isac.bazar_solidario.model.enums.StatusAnuncio;
import com.isac.bazar_solidario.repository.*;
import com.isac.bazar_solidario.dto.*;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.*;
import java.math.*;
import java.util.*;
@Service @RequiredArgsConstructor
public class PedidoService {
 private final PedidoRepository pedidos;
 private final AnuncioRepository anuncios;
 private final EntityManager em;
 private void exigir(boolean ok,HttpStatus status,String texto) {if(!ok) throw new ResponseStatusException(status,texto);}
 @Transactional public PedidoResponse criar(Usuario cliente,String chave,PedidoRequest req) {
  exigir(cliente.getRole()==Role.CLIENTE,HttpStatus.FORBIDDEN,"Somente clientes podem fazer pedidos.");
  exigir(chave!=null && chave.matches("[A-Za-z0-9-]{16,64}"),HttpStatus.BAD_REQUEST,"Chave de confirmação inválida.");
  // Serializa tentativas do mesmo cliente, inclusive cliques/requisições simultâneas.
  em.find(Usuario.class,cliente.getId(),LockModeType.PESSIMISTIC_WRITE);
  var quantidades=new TreeMap<Long,Integer>();
  for(var item:req.itens()) exigir(quantidades.putIfAbsent(item.anuncioId(),item.quantidade())==null,HttpStatus.BAD_REQUEST,"Peça repetida no pedido.");
  String assinatura=quantidades.toString();
  var existente=pedidos.findByClienteIdAndChave(cliente.getId(),chave);
  if(existente.isPresent()) {exigir(existente.get().getRequisicao().equals(assinatura),HttpStatus.CONFLICT,"Esta confirmação já foi usada para outra sacola.");return PedidoResponse.de(existente.get());}
  Pedido p=new Pedido();p.setCliente(cliente);p.setChave(chave);p.setRequisicao(assinatura);
  BigDecimal total=BigDecimal.ZERO;
  // Ordem crescente evita bloqueios cruzados em sacolas com várias peças.
  for(var entrada:quantidades.entrySet()) {
   Anuncio a=anuncios.bloquear(entrada.getKey()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Peça não encontrada."));
   exigir(a.getStatus()==StatusAnuncio.ATIVO && a.getEstoque()>=entrada.getValue(),HttpStatus.CONFLICT,"Peça indisponível ou estoque insuficiente: "+a.getTitulo());
   if(p.getVendedor()==null)p.setVendedor(a.getVendedor());
   exigir(p.getVendedor().getId().equals(a.getVendedor().getId()),HttpStatus.BAD_REQUEST,"Faça um pedido por brechó.");
   ItemPedido i=new ItemPedido();i.setPedido(p);i.setAnuncio(a);i.setTitulo(a.getTitulo());i.setQuantidade(entrada.getValue());i.setPrecoUnitario(a.getPreco());p.getItens().add(i);
   total=total.add(a.getPreco().multiply(BigDecimal.valueOf(entrada.getValue())));
   a.alterarEstoque(a.getEstoque()-entrada.getValue());
  }
  p.setTotal(total.setScale(2,RoundingMode.HALF_UP));
  p.setValorIsac(total.multiply(new BigDecimal("0.10")).setScale(2,RoundingMode.HALF_UP));
  p.setValorBrecho(p.getTotal().subtract(p.getValorIsac()));
  return PedidoResponse.de(pedidos.save(p));
 }
 @Transactional(readOnly=true) public PageResponse<PedidoResponse> listar(Usuario u,Pageable page) {
  var resultado=u.getRole()==Role.LOJA?pedidos.findByVendedorIdOrderByIdDesc(u.getId(),page):pedidos.findByClienteIdOrderByIdDesc(u.getId(),page);
  return PageResponse.de(resultado.map(PedidoResponse::de));
 }
 @Transactional public PedidoResponse mudar(Long id,String acao,Usuario u) {
  Pedido p=pedidos.bloquear(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Pedido não encontrado."));
  boolean dono=p.getVendedor().getId().equals(u.getId()) && u.getRole()==Role.LOJA;
  boolean comprador=p.getCliente().getId().equals(u.getId()) && u.getRole()==Role.CLIENTE;
  exigir(dono||comprador,HttpStatus.FORBIDDEN,"Este pedido não pertence à sua conta.");
  if(acao.equals("confirmar")) {
   exigir(dono,HttpStatus.FORBIDDEN,"A confirmação é feita pelo brechó.");
   exigir(p.getEstado().equals("AGUARDANDO_CONFIRMACAO_MANUAL"),HttpStatus.CONFLICT,"Pedido já atualizado.");
   p.setEstado("CONFIRMADO"); // Aceite comercial; não declara pagamento recebido.
  } else if(acao.equals("cancelar")) {
   if(p.getEstado().equals("CANCELADO"))return PedidoResponse.de(p);
   exigir(p.getEstado().equals("AGUARDANDO_CONFIRMACAO_MANUAL") || dono,HttpStatus.CONFLICT,"Peça ao brechó para cancelar o pedido confirmado.");
   p.getItens().stream().sorted(Comparator.comparing(i->i.getAnuncio().getId())).forEach(i->{var a=anuncios.bloquear(i.getAnuncio().getId()).orElseThrow();a.alterarEstoque(Math.addExact(a.getEstoque(),i.getQuantidade()));});
   p.setEstado("CANCELADO");
  } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Ação inválida.");
  return PedidoResponse.de(p);
 }
}
