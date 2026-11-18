package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.config.MercadoLivreConfig;
import com.isac.bazar_solidario.model.*;
import com.isac.bazar_solidario.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import java.security.*;
import java.time.Instant;
import java.util.*;
import java.nio.charset.StandardCharsets;

@Service
public class MercadoLivreService {
    private final MercadoLivreConfig config;
    private final MercadoLivreGateway gateway;
    private final IntegracaoSegredos segredos;
    private final MercadoLivreContaRepository contas;
    private final MercadoLivreAutorizacaoRepository autorizacoes;
    private final MercadoLivreVinculoRepository vinculos;
    private final AnuncioRepository anuncios;
    private final TransactionTemplate tx;
    private final SecureRandom random=new SecureRandom();
    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager em;
    @org.springframework.beans.factory.annotation.Autowired private MercadoLivrePublicacaoRepository publicacoes;

    public MercadoLivreService(MercadoLivreConfig config,MercadoLivreGateway gateway,IntegracaoSegredos segredos,
            MercadoLivreContaRepository contas,MercadoLivreAutorizacaoRepository autorizacoes,
            MercadoLivreVinculoRepository vinculos,AnuncioRepository anuncios,PlatformTransactionManager manager) {
        this.config=config;this.gateway=gateway;this.segredos=segredos;this.contas=contas;
        this.autorizacoes=autorizacoes;this.vinculos=vinculos;this.anuncios=anuncios;this.tx=new TransactionTemplate(manager);
    }
    private void exigir(boolean ok,HttpStatus status,String mensagem) { if(!ok)throw new ResponseStatusException(status,mensagem); }
    private void loja(Usuario usuario) { exigir(usuario.getRole()==Role.LOJA,HttpStatus.FORBIDDEN,"Integração exclusiva para brechós."); }
    private void configurado() { exigir(config.configurado(),HttpStatus.SERVICE_UNAVAILABLE,"Configure a aplicação Mercado Livre no servidor antes de conectar."); }
    private String aleatorio() { byte[] bytes=new byte[32];random.nextBytes(bytes);return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    private static byte[] digest(String texto) {
        try {return MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));}
        catch(NoSuchAlgorithmException error) {throw new IllegalStateException("SHA-256 indisponível.");}
    }
    static String hash(String texto) { return HexFormat.of().formatHex(digest(texto)); }

    public Map<String,Object> situacao(Usuario usuario) {
        loja(usuario);
        var conta=contas.findById(usuario.getId());
        var result=new LinkedHashMap<String,Object>();
        result.put("configurado",config.configurado());result.put("somenteTeste",true);result.put("conectado",conta.isPresent());
        conta.ifPresent(c->{result.put("apelido",c.getApelido());result.put("vendedorExterno",c.getVendedorExterno());});
        return result;
    }
    public String iniciar(Usuario usuario) {
        loja(usuario);configurado();
        String state=aleatorio(),verifier=aleatorio();
        tx.executeWithoutResult(s->{
            autorizacoes.deleteByExpiraEmBefore(Instant.now());
            var entrada=new MercadoLivreAutorizacao();entrada.setHash(hash(state));entrada.setUsuarioId(usuario.getId());
            entrada.setExpiraEm(Instant.now().plusSeconds(600));entrada.setVerificadorCifrado(segredos.cifrar(verifier));
            autorizacoes.save(entrada);
        });
        return "https://auth.mercadolivre.com.br/authorization?response_type=code&client_id="+config.getClientId()
            +"&redirect_uri="+MercadoLivreGateway.encode(config.getRedirectUri())+"&state="+state
            +"&code_challenge="+Base64.getUrlEncoder().withoutPadding().encodeToString(digest(verifier))+"&code_challenge_method=S256";
    }
    public void concluir(String state,String code) {
        configurado();
        exigir(state!=null && state.matches("[A-Za-z0-9_-]{43}") && code!=null && !code.isBlank() && code.length()<=2048,
            HttpStatus.BAD_REQUEST,"Retorno OAuth inválido. Inicie uma nova conexão pelo aplicativo.");
        // Consome o state antes da troca externa, inclusive quando o provedor recusa o código.
        var entrada=tx.execute(s->{
            var a=autorizacoes.bloquear(hash(state)).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Autorização expirada ou já utilizada."));
            autorizacoes.delete(a);return a;
        });
        exigir(entrada.getExpiraEm().isAfter(Instant.now()),HttpStatus.BAD_REQUEST,"Autorização expirada. Inicie novamente.");
        var resposta=gateway.token(Map.of("grant_type","authorization_code","client_id",config.getClientId(),
            "client_secret",config.getClientSecret(),"code",code,"redirect_uri",config.getRedirectUri(),
            "code_verifier",segredos.abrir(entrada.getVerificadorCifrado())));
        var me=gateway.get("/users/me",resposta.path("access_token").asText());
        validarContaTeste(me);
        exigir(resposta.path("user_id").asLong()==me.path("id").asLong(),HttpStatus.BAD_GATEWAY,"Conta OAuth divergente.");
        tx.executeWithoutResult(s->{
            var conta=contas.bloquear(entrada.getUsuarioId()).orElseGet(MercadoLivreConta::new);
            exigir(conta.getVendedorExterno()==null || conta.getVendedorExterno()==me.path("id").asLong(),
                HttpStatus.CONFLICT,"Esta loja já está vinculada a outra conta. Desconecte antes de trocar.");
            conta.setUsuarioId(entrada.getUsuarioId());conta.setVendedorExterno(me.path("id").asLong());
            conta.setApelido(me.path("nickname").asText());atualizarTokens(conta,resposta);contas.save(conta);
        });
    }
    static void validarContaTeste(JsonNode me) {
        boolean teste=false;
        for(var tag:me.path("tags")) if("test_user".equals(tag.asText()))teste=true;
        if(!teste || !"MLB".equals(me.path("site_id").asText()) || me.path("id").asLong()<=0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Este piloto aceita somente usuários de teste do Mercado Livre Brasil.");
    }
    private void atualizarTokens(MercadoLivreConta conta,JsonNode resposta) {
        String access=resposta.path("access_token").asText(),refresh=resposta.path("refresh_token").asText();
        long validade=resposta.path("expires_in").asLong();
        exigir(!access.isBlank() && !refresh.isBlank() && validade>60,HttpStatus.BAD_GATEWAY,"Credenciais incompletas recebidas do provedor.");
        conta.setAccessTokenCifrado(segredos.cifrar(access));conta.setRefreshTokenCifrado(segredos.cifrar(refresh));
        conta.setExpiraEm(Instant.now().plusSeconds(validade));
    }
    private record Acesso(String token,long vendedor) {}
    private Acesso acesso(Usuario usuario) {
        loja(usuario);configurado();
        return tx.execute(s->{
            var conta=contas.bloquear(usuario.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.CONFLICT,"Conecte sua conta de teste primeiro."));
            if(conta.getExpiraEm().isBefore(Instant.now().plusSeconds(60))) {
                var novo=gateway.token(Map.of("grant_type","refresh_token","client_id",config.getClientId(),
                    "client_secret",config.getClientSecret(),"refresh_token",segredos.abrir(conta.getRefreshTokenCifrado())));
                exigir(novo.path("user_id").asLong()==conta.getVendedorExterno(),HttpStatus.BAD_GATEWAY,"Renovação retornou outra conta.");
                atualizarTokens(conta,novo);
            }
            return new Acesso(segredos.abrir(conta.getAccessTokenCifrado()),conta.getVendedorExterno());
        });
    }
    public JsonNode listar(Usuario usuario) {
        var acesso=acesso(usuario);
        return gateway.get("/users/"+acesso.vendedor()+"/items/search?limit=50",acesso.token());
    }
    private JsonNode item(String itemId,Acesso acesso) {
        exigir(itemId!=null && itemId.matches("MLB[0-9]{6,20}"),HttpStatus.BAD_REQUEST,"Informe um código de anúncio MLB válido.");
        var item=gateway.get("/items/"+itemId,acesso.token());
        exigir(item.path("seller_id").asLong()==acesso.vendedor(),HttpStatus.FORBIDDEN,"O anúncio não pertence à conta conectada.");
        return item;
    }
    public JsonNode consultarItem(String itemId,Usuario usuario) {return item(itemId,acesso(usuario));}
    public JsonNode frete(String itemId,String cep,Usuario usuario) {
        exigir(cep!=null && cep.matches("[0-9]{8}"),HttpStatus.BAD_REQUEST,"Informe um CEP com oito dígitos.");
        var acesso=acesso(usuario);item(itemId,acesso);
        return gateway.get("/items/"+itemId+"/shipping_options?zip_code="+cep,acesso.token());
    }
    public MercadoLivreVinculo vincular(Long anuncioId,String itemId,Usuario usuario) {
        var acesso=acesso(usuario);var item=item(itemId,acesso);
        return tx.execute(s->{
            var anuncio=anuncios.bloquear(anuncioId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Peça não encontrada."));
            exigir(anuncio.pertenceA(usuario.getId()),HttpStatus.FORBIDDEN,"Peça de outro brechó.");
            exigir(anuncio.getStatus()==com.isac.bazar_solidario.model.enums.StatusAnuncio.INATIVO,HttpStatus.CONFLICT,
                "Oculte a peça no ISAC antes de vincular ao canal Mercado Livre. A sincronização automática entre canais não está habilitada.");
            long reservas=em.createQuery("select count(i) from ItemPedido i where i.anuncio.id=:id and i.pedido.estado <> 'CANCELADO'",Long.class)
                .setParameter("id",anuncioId).getSingleResult();
            exigir(reservas==0,HttpStatus.CONFLICT,"Esta peça já possui pedidos no ISAC. Resolva esses pedidos antes de trocar de canal.");
            var vinculo=vinculos.findById(anuncioId).orElseGet(MercadoLivreVinculo::new);
            exigir(vinculo.getItemId()==null || vinculo.getItemId().equals(itemId),HttpStatus.CONFLICT,"Peça já vinculada a outro anúncio.");
            vinculo.setAnuncioId(anuncioId);vinculo.setUsuarioId(usuario.getId());vinculo.setItemId(itemId);
            vinculo.setStatusExterno(item.path("status").asText());vinculo.setEstoqueExterno(item.path("available_quantity").asInt());
            vinculo.setConsultadoEm(Instant.now());return vinculos.save(vinculo);
        });
    }
    public List<MercadoLivreVinculo> vinculos(Usuario usuario) {loja(usuario);return vinculos.findByUsuarioIdOrderByAnuncioId(usuario.getId());}

    public JsonNode atributos(String categoria,Usuario usuario) {
        exigir(categoria!=null && categoria.matches("MLB[0-9]{2,20}"),HttpStatus.BAD_REQUEST,"Informe uma categoria Mercado Livre Brasil válida.");
        return gateway.get("/categories/"+categoria+"/attributes",acesso(usuario).token());
    }
    public MercadoLivrePublicacao publicar(Long anuncioId,String categoria,String tipo,Map<String,String> valores,Usuario usuario) {
        var acesso=acesso(usuario);
        var me=gateway.get("/users/me",acesso.token());validarContaTeste(me);
        exigir(me.path("id").asLong()==acesso.vendedor(),HttpStatus.CONFLICT,"Conta conectada divergente.");
        exigir(Set.of("free","gold_special").contains(tipo),HttpStatus.BAD_REQUEST,"Tipo de anúncio não suportado no piloto.");
        var definicoes=atributos(categoria,usuario);
        java.net.URI origem;
        try {origem=java.net.URI.create(config.getPublicAppUrl());}
        catch(RuntimeException error) {throw new ResponseStatusException(HttpStatus.CONFLICT,"Configure a URL pública do aplicativo para publicar fotos.");}
        exigir("https".equals(origem.getScheme()) && origem.getHost()!=null && origem.getUserInfo()==null
            && (origem.getPath().isEmpty() || origem.getPath().equals("/")) && origem.getQuery()==null && origem.getFragment()==null,
            HttpStatus.CONFLICT,"Configure PUBLIC_APP_URL com a origem HTTPS do aplicativo.");
        String origemPublica=origem.getScheme()+"://"+origem.getAuthority();
        var corpo=tx.execute(s->{
            var a=anuncios.bloquear(anuncioId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Peça não encontrada."));
            exigir(a.pertenceA(usuario.getId()),HttpStatus.FORBIDDEN,"Peça de outro brechó.");
            exigir(!vinculos.existsById(anuncioId) || publicacoes.existsById(anuncioId),HttpStatus.CONFLICT,"Esta peça já possui anúncio associado no Mercado Livre.");
            exigir(a.getStatus()==com.isac.bazar_solidario.model.enums.StatusAnuncio.INATIVO && a.getEstoque()>0,HttpStatus.CONFLICT,"Oculte uma peça com estoque antes de publicar no outro canal.");
            exigir(em.createQuery("select count(i) from ItemPedido i where i.anuncio.id=:id and i.pedido.estado <> 'CANCELADO'",Long.class)
                .setParameter("id",anuncioId).getSingleResult()==0,HttpStatus.CONFLICT,"Resolva os pedidos desta peça antes de trocar de canal.");
            exigir(!a.getImagens().isEmpty(),HttpStatus.BAD_REQUEST,"Adicione uma foto à peça antes de publicar.");
            var atributos=new ArrayList<Map<String,String>>();
            for(var definicao:definicoes) {
                String id=definicao.path("id").asText();
                if(definicao.path("tags").path("read_only").asBoolean())continue;
                String valor=valores==null?null:valores.get(id);
                if("ITEM_CONDITION".equals(id) && (valor==null || valor.isBlank())) {
                    for(var opcao:definicao.path("values")) if(Set.of("usado","used").contains(opcao.path("name").asText().toLowerCase(Locale.ROOT)))valor=opcao.path("id").asText();
                }
                if(valor!=null && !valor.isBlank()) {
                    exigir(valor.length()<=250,HttpStatus.BAD_REQUEST,"Atributo muito longo: "+id);
                    String chave="value_name";
                    for(var opcao:definicao.path("values")) if(opcao.path("id").asText().equals(valor))chave="value_id";
                    atributos.add(Map.of("id",id,chave,valor));
                } else exigir(!definicao.path("tags").path("required").asBoolean(),HttpStatus.BAD_REQUEST,"Preencha o atributo: "+definicao.path("name").asText());
            }
            var fotos=new ArrayList<Map<String,String>>();
            for(var foto:a.getImagens()) {
                exigir(foto.getUrl().matches("/uploads/[a-f0-9-]{36}\\.(jpg|png|webp)"),HttpStatus.BAD_REQUEST,"Envie as fotos pelo aplicativo antes de publicar no Mercado Livre.");
                fotos.add(Map.of("source",origemPublica+"/api"+foto.getUrl()));
            }
            var dados=new LinkedHashMap<String,Object>();
            boolean up=false;for(var tag:me.path("tags"))if("user_product_seller".equals(tag.asText()))up=true;
            dados.put(up?"family_name":"title","Item de Teste - Por favor, NAO OFERTAR!");
            dados.put("site_id","MLB");dados.put("category_id",categoria);dados.put("price",a.getPreco());
            dados.put("currency_id","BRL");dados.put("available_quantity",a.getEstoque());
            dados.put("buying_mode","buy_it_now");dados.put("listing_type_id",tipo);
            dados.put("pictures",fotos);dados.put("attributes",atributos);
            return dados;
        });
        var anterior=publicacoes.findById(anuncioId);
        if(anterior.isPresent()) {exigir(anterior.get().getUsuarioId().equals(usuario.getId()),HttpStatus.FORBIDDEN,"Peça de outro brechó.");return anterior.get();}
        gateway.post("/items/validate",acesso.token(),corpo);
        boolean nova=Boolean.TRUE.equals(tx.execute(s->{
            var a=anuncios.bloquear(anuncioId).orElseThrow();
            exigir(a.getStatus()==com.isac.bazar_solidario.model.enums.StatusAnuncio.INATIVO && a.getEstoque().equals(corpo.get("available_quantity"))
                && a.getPreco().equals(corpo.get("price")),HttpStatus.CONFLICT,"A peça mudou. Valide novamente antes de publicar.");
            if(publicacoes.existsById(anuncioId))return false;
            var p=new MercadoLivrePublicacao();p.setAnuncioId(anuncioId);p.setUsuarioId(usuario.getId());p.setEstado("ENVIANDO");
            p.setAviso("Tentativa registrada. Não repita se a resposta for inconclusiva.");p.setAtualizadoEm(Instant.now());publicacoes.save(p);return true;
        }));
        if(nova)try {
            var publicado=gateway.post("/items",acesso.token(),corpo);
            String id=publicado.path("id").asText();
            exigir(id.matches("MLB[0-9]{6,20}") && publicado.path("seller_id").asLong()==acesso.vendedor(),HttpStatus.BAD_GATEWAY,"Confirmação da publicação divergente.");
            vincular(anuncioId,id,usuario);
            tx.executeWithoutResult(s->{var p=publicacoes.findById(anuncioId).orElseThrow();p.setItemId(id);p.setEstado("PUBLICADO");p.setAviso("Anúncio de teste publicado. A peça permanece oculta no ISAC.");p.setAtualizadoEm(Instant.now());});
        } catch(RuntimeException error) {
            tx.executeWithoutResult(s->{var p=publicacoes.findById(anuncioId).orElseThrow();p.setEstado("RECONCILIAR");p.setAviso("Consulte seus anúncios no Mercado Livre e associe o código encontrado. Uma nova cobrança de publicação não será tentada automaticamente.");p.setAtualizadoEm(Instant.now());});
        }
        return publicacoes.findById(anuncioId).orElseThrow();
    }
}
