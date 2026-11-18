package com.isac.bazar_solidario.controller;

import com.isac.bazar_solidario.service.MercadoLivreGateway;
import com.isac.bazar_solidario.repository.MercadoLivreContaRepository;
import tools.jackson.databind.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.util.*;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
@TestPropertySource(properties={"spring.datasource.url=jdbc:h2:mem:integracoes_test;DB_CLOSE_DELAY=-1",
    "ML_CLIENT_ID=123456", "ML_CLIENT_SECRET=segredo-ficticio-apenas-teste",
    "ML_REDIRECT_URI=https://isac.example/api/integracoes/mercadolivre/callback",
    "ML_ENCRYPTION_KEY=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", "PUBLIC_APP_URL=https://isac.example", "app.uploads.storage=database"})
class IntegracoesIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired MercadoLivreContaRepository contas;
    @Autowired com.isac.bazar_solidario.repository.CategoriaRepository categorias;
    @MockitoBean MercadoLivreGateway gateway;
    final ObjectMapper json=new ObjectMapper();
    record Conta(String token,long id){}
    Conta conta(String tipo) throws Exception {
        var resposta=mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content("""
            {"nome":"Loja de teste","email":"%s@test.local","senha":"Senha2026!","tipo":"%s","whatsapp":"5581999999999"}
            """.formatted(UUID.randomUUID(),tipo))).andExpect(status().isCreated()).andReturn();
        var dados=json.readTree(resposta.getResponse().getContentAsString());
        return new Conta(dados.path("token").asText(),dados.path("usuario").path("id").asLong());
    }
    Map<String,String> iniciar(Conta loja) throws Exception {
        var resposta=mvc.perform(post("/integracoes/mercadolivre/conectar").header("Authorization","Bearer "+loja.token()))
            .andExpect(status().isOk()).andReturn();
        var url=URI.create(json.readTree(resposta.getResponse().getContentAsString()).path("url").asText());
        assertEquals("auth.mercadolivre.com.br",url.getHost());
        return Arrays.stream(url.getRawQuery().split("&")).map(s->s.split("=",2))
            .collect(Collectors.toMap(p->p[0],p->URLDecoder.decode(p[1],StandardCharsets.UTF_8)));
    }
    long prepararConta() throws Exception {
        long externo=Math.abs(UUID.randomUUID().getMostSignificantBits()>>>1);
        when(gateway.token(anyMap())).thenReturn(json.readTree("""
            {"access_token":"access-nao-real","refresh_token":"refresh-nao-real","user_id":%d,"expires_in":21600}
            """.formatted(externo)));
        when(gateway.get(eq("/users/me"),anyString())).thenReturn(json.readTree("""
            {"id":%d,"nickname":"TESTISAC","site_id":"MLB","tags":["test_user"]}
            """.formatted(externo)));
        return externo;
    }
    long pecaOculta(Conta loja) throws Exception {
        var body=new LinkedHashMap<String,Object>();
        body.put("titulo","Peça para teste "+UUID.randomUUID());body.put("preco",25);body.put("estoque",1);
        body.put("categoriaId",categorias.findAll().getFirst().getId());
        body.put("imagens",List.of("/uploads/"+UUID.randomUUID()+".png"));
        var res=mvc.perform(post("/anuncios").header("Authorization","Bearer "+loja.token())
            .header("Idempotency-Key",UUID.randomUUID().toString()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
            .andExpect(status().isCreated()).andReturn();
        long id=json.readTree(res.getResponse().getContentAsString()).path("id").asLong();
        mvc.perform(patch("/anuncios/"+id+"/desativar").header("Authorization","Bearer "+loja.token())).andExpect(status().isOk());
        return id;
    }
    Conta conectarPublicacao() throws Exception {
        var loja=conta("LOJA");long externo=prepararConta();var params=iniciar(loja);
        mvc.perform(get("/integracoes/mercadolivre/callback").param("state",params.get("state")).param("code","codigo-ficticio"))
            .andExpect(status().isOk());
        when(gateway.get(eq("/users/me"),anyString())).thenReturn(json.readTree("""
            {"id":%d,"site_id":"MLB","tags":["test_user","user_product_seller"]}
            """.formatted(externo)));
        when(gateway.get(eq("/categories/MLB1234/attributes"),anyString())).thenReturn(json.readTree("[]"));
        when(gateway.post(eq("/items/validate"),anyString(),any())).thenReturn(json.readTree("{}"));
        var item=json.readTree("{\"id\":\"MLB1234567890\",\"seller_id\":"+externo+",\"status\":\"active\",\"available_quantity\":1}");
        when(gateway.post(eq("/items"),anyString(),any())).thenReturn(item);
        when(gateway.get(eq("/items/MLB1234567890"),anyString())).thenReturn(item);
        return loja;
    }
    org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder publicar(Conta loja,long id) {
        return post("/integracoes/mercadolivre/publicacoes").header("Authorization","Bearer "+loja.token())
            .contentType(MediaType.APPLICATION_JSON).content("{\"anuncioId\":"+id+",\"categoriaId\":\"MLB1234\",\"tipoAnuncio\":\"free\",\"atributos\":{}}");
    }
    @Test void publicacaoUsaDadosDoServidorENaoDuplicaAnuncioNemReativaCanalLocal() throws Exception {
        var loja=conectarPublicacao();long id=pecaOculta(loja);
        mvc.perform(publicar(loja,id)).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("PUBLICADO"));
        mvc.perform(publicar(loja,id)).andExpect(status().isOk()).andExpect(jsonPath("$.itemId").value("MLB1234567890"));
        var body=org.mockito.ArgumentCaptor.forClass(Object.class);
        verify(gateway,times(1)).post(eq("/items"),anyString(),body.capture());
        var dados=json.valueToTree(body.getValue());
        assertTrue(dados.has("family_name"));assertFalse(dados.has("title"));
        assertEquals(25,dados.path("price").asInt());assertEquals(1,dados.path("available_quantity").asInt());
        mvc.perform(patch("/anuncios/"+id+"/ativar").header("Authorization","Bearer "+loja.token())).andExpect(status().isConflict());
    }
    @Test void respostaInconclusivaBloqueiaRepeticaoEReativacaoDaPeca() throws Exception {
        var loja=conectarPublicacao();long id=pecaOculta(loja);
        when(gateway.post(eq("/items"),anyString(),any())).thenThrow(new IllegalStateException("timeout fictício"));
        mvc.perform(publicar(loja,id)).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("RECONCILIAR"));
        mvc.perform(publicar(loja,id)).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("RECONCILIAR"));
        verify(gateway,times(1)).post(eq("/items"),anyString(),any());
        mvc.perform(patch("/anuncios/"+id+"/ativar").header("Authorization","Bearer "+loja.token())).andExpect(status().isConflict());
    }
    @Test void healthPublicoIncluiBancoSemExporCredenciais() throws Exception {
        mvc.perform(get("/health")).andExpect(status().isOk()).andExpect(jsonPath("$.database").value("UP"));
        mvc.perform(get("/health/live")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }
    @Test void prefixoApiPreservaAutenticacaoENavegacaoHtmlNaoViraErroDeApi() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.database").value("UP"));
        mvc.perform(get("/api/integracoes/mercadolivre/situacao")).andExpect(status().isUnauthorized());
        mvc.perform(get("/perfil").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }
    @Test void fotoNoBancoMantemConteudoSemDependerDoDiscoDoContainer() throws Exception {
        var loja=conta("LOJA");
        var image=new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB);
        var bytes=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(image,"png",bytes);
        var arquivo=new org.springframework.mock.web.MockMultipartFile("arquivo","foto.png","image/png",bytes.toByteArray());
        var resposta=mvc.perform(multipart("/api/uploads").file(arquivo).header("Authorization","Bearer "+loja.token()))
            .andExpect(status().isOk()).andReturn();
        var url=json.readTree(resposta.getResponse().getContentAsString()).path("url").asText();
        mvc.perform(get("/api"+url)).andExpect(status().isOk()).andExpect(content().bytes(bytes.toByteArray()))
            .andExpect(header().string("X-Content-Type-Options","nosniff"));
    }
    @Test void oauthUsaPkceStateUnicoECifraCredenciais() throws Exception {
        var loja=conta("LOJA");prepararConta();var params=iniciar(loja);
        assertEquals("S256",params.get("code_challenge_method"));assertEquals(43,params.get("code_challenge").length());
        mvc.perform(get("/integracoes/mercadolivre/callback").param("state",params.get("state")).param("code","codigo-ficticio"))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"));
        var salvo=contas.findById(loja.id()).orElseThrow();
        assertFalse(salvo.getAccessTokenCifrado().contains("access-nao-real"));
        var resposta=mvc.perform(get("/integracoes/mercadolivre/situacao").header("Authorization","Bearer "+loja.token()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.conectado").value(true)).andReturn().getResponse().getContentAsString();
        assertFalse(resposta.contains("access-nao-real"));assertFalse(resposta.contains("refresh-nao-real"));
        mvc.perform(get("/integracoes/mercadolivre/callback").param("state",params.get("state")).param("code","codigo-ficticio"))
            .andExpect(status().isBadRequest());
        verify(gateway,times(1)).token(anyMap());
    }
    @Test void usuarioClienteEAnonimoNaoAcessamIntegracao() throws Exception {
        mvc.perform(get("/integracoes/mercadolivre/situacao")).andExpect(status().isUnauthorized());
        var cliente=conta("CLIENTE");
        mvc.perform(post("/integracoes/mercadolivre/conectar").header("Authorization","Bearer "+cliente.token()))
            .andExpect(status().isForbidden());
        verifyNoInteractions(gateway);
    }
    @Test void recusaContaRealMesmoQueOAuthRetorneToken() throws Exception {
        var loja=conta("LOJA");long externo=prepararConta();var params=iniciar(loja);
        when(gateway.get(eq("/users/me"),anyString())).thenReturn(json.readTree("{\"id\":"+externo+",\"site_id\":\"MLB\",\"tags\":[\"normal\"]}"));
        mvc.perform(get("/integracoes/mercadolivre/callback").param("state",params.get("state")).param("code","codigo-ficticio"))
            .andExpect(status().isConflict());
        assertTrue(contas.findById(loja.id()).isEmpty());
    }
    @Test void validaCepEPropriedadeDoAnuncioAntesDeConsultarFrete() throws Exception {
        var loja=conta("LOJA");long externo=prepararConta();var params=iniciar(loja);
        mvc.perform(get("/integracoes/mercadolivre/callback").param("state",params.get("state")).param("code","codigo-ficticio"))
            .andExpect(status().isOk());
        when(gateway.get(eq("/items/MLB1234567890"),anyString())).thenReturn(json.readTree("{\"seller_id\":"+(externo+1)+"}"));
        mvc.perform(get("/integracoes/mercadolivre/itens/MLB1234567890/frete").param("cep","50000000").header("Authorization","Bearer "+loja.token()))
            .andExpect(status().isForbidden());
        mvc.perform(get("/integracoes/mercadolivre/itens/MLB1234567890/frete").param("cep","../").header("Authorization","Bearer "+loja.token()))
            .andExpect(status().isBadRequest());
        verify(gateway,never()).get(contains("shipping_options"),anyString());
        when(gateway.get(eq("/items/MLB1234567890"),anyString())).thenReturn(json.readTree("{\"seller_id\":"+externo+"}"));
        when(gateway.get(eq("/items/MLB1234567890/shipping_options?zip_code=50000000"),anyString()))
            .thenReturn(json.readTree("{\"options\":[{\"id\":1,\"name\":\"Teste\",\"cost\":12.5}]}"));
        mvc.perform(get("/integracoes/mercadolivre/itens/MLB1234567890/frete").param("cep","50000000").header("Authorization","Bearer "+loja.token()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.options[0].cost").value(12.5));
    }
}
