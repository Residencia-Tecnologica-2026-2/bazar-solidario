package com.isac.bazar_solidario.controller;

import tools.jackson.databind.*;
import com.isac.bazar_solidario.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;
import java.util.*;
import java.util.concurrent.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
@TestPropertySource(properties="spring.datasource.url=jdbc:h2:mem:anuncios_duplicados;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000")
class AnunciosDuplicadosIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired CategoriaRepository categorias;
    @Autowired AnuncioRepository anuncios;
    final ObjectMapper json=new ObjectMapper();
    String loja() throws Exception {
        var res=mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content("""
            {"nome":"Loja teste","email":"%s@test.local","senha":"Senha2026!","tipo":"LOJA","whatsapp":"5581999999999"}
            """.formatted(UUID.randomUUID()))).andExpect(status().isCreated()).andReturn();
        return json.readTree(res.getResponse().getContentAsString()).path("token").asText();
    }
    String dados(String titulo,String codigo,boolean confirmar,List<String> imagens) {
        var body=new LinkedHashMap<String,Object>();body.put("titulo",titulo);body.put("preco",25);body.put("estoque",1);
        body.put("categoriaId",categorias.findAll().get(0).getId());body.put("codigoPeca",codigo);
        body.put("confirmarSemelhante",confirmar);body.put("imagens",imagens);
        return json.writeValueAsString(body);
    }
    org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder publicar(String token,String chave,String dados){
        return post("/anuncios").header("Authorization","Bearer "+token).header("Idempotency-Key",chave)
            .contentType(MediaType.APPLICATION_JSON).content(dados);
    }
    JsonNode criado(String token,String chave,String dados) throws Exception {
        return json.readTree(mvc.perform(publicar(token,chave,dados)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }
    @Test void repeticaoRetornaMesmoAnuncioEDadosDiferentesSaoRecusados() throws Exception {
        var token=loja();var chave=UUID.randomUUID().toString();var dados=dados("Camisa de linho",null,false,List.of());
        var a=criado(token,chave,dados);var b=criado(token,chave,dados);
        assertEquals(a.path("id").asLong(),b.path("id").asLong());
        assertTrue(a.path("codigoPeca").asText().startsWith("PEC-"));
        mvc.perform(publicar(token,chave,dados("Outra peça",null,false,List.of()))).andExpect(status().isConflict());
    }
    @Test void codigoUnicoPorLojaMesmoConfirmandoOutraPeca() throws Exception {
        var token=loja();criado(token,UUID.randomUUID().toString(),dados("Camisa","CAM-001",false,List.of()));
        mvc.perform(publicar(token,UUID.randomUUID().toString(),dados("Saia","cam-001",true,List.of())))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(org.hamcrest.Matchers.containsString("código")));
        criado(loja(),UUID.randomUUID().toString(),dados("Camisa","CAM-001",false,List.of()));
    }
    @Test void alertaSemCriarEPermiteConfirmarPecaDiferente() throws Exception {
        var token=loja();var a=criado(token,UUID.randomUUID().toString(),dados("Tênis  casual",null,false,List.of()));
        var chave=UUID.randomUUID().toString();
        mvc.perform(publicar(token,chave,dados("TENIS CASUAL",null,false,List.of())))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("ANUNCIO_SEMELHANTE"))
            .andExpect(jsonPath("$.semelhantes[0].id").value(a.path("id").asLong()));
        var b=criado(token,chave,dados("TENIS CASUAL",null,true,List.of()));
        assertNotEquals(a.path("codigoPeca").asText(),b.path("codigoPeca").asText());
        assertEquals(b.path("id").asLong(),criado(token,chave,dados("TENIS CASUAL",null,true,List.of())).path("id").asLong());
    }
    @Test void duasPublicacoesSimultaneasProduzemUmRegistro() throws Exception {
        var token=loja();var chave=UUID.randomUUID().toString();var body=dados("Vestido azul",null,false,List.of());
        var inicio=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
        try{
            Callable<Long> publicar=()->{inicio.await();return criado(token,chave,body).path("id").asLong();};
            var a=pool.submit(publicar);var b=pool.submit(publicar);inicio.countDown();
            assertEquals(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS));
        }finally{pool.shutdownNow();}
    }
    String foto(String token,byte[] imagem) throws Exception {
        var res=mvc.perform(multipart("/uploads").file(new MockMultipartFile("arquivo",UUID.randomUUID()+".png","image/png",imagem))
            .header("Authorization","Bearer "+token)).andExpect(status().isOk()).andReturn();
        return json.readTree(res.getResponse().getContentAsString()).path("url").asText();
    }
    @Test void mesmaImagemReenviadaComOutroNomeGeraAviso() throws Exception {
        var token=loja();var imagem=new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);var bytes=new ByteArrayOutputStream();
        ImageIO.write(imagem,"png",bytes);
        String foto1=foto(token,bytes.toByteArray()),foto2=foto(token,bytes.toByteArray());
        assertNotEquals(foto1,foto2);
        criado(token,UUID.randomUUID().toString(),dados("Bolsa marrom",null,false,List.of(foto1)));
        mvc.perform(publicar(token,UUID.randomUUID().toString(),dados("Outro título",null,false,List.of(foto2))))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.semelhantes[0].motivos[0]").value("Mesma foto"));
    }
    @Test void naoExibeAnunciosDeOutraLojaENaoAceitaPostSemChave() throws Exception {
        String a=loja(),b=loja();String body=dados("Camisa preta",null,false,List.of());
        criado(a,UUID.randomUUID().toString(),body);criado(b,UUID.randomUUID().toString(),body);
        mvc.perform(post("/anuncios").header("Authorization","Bearer "+a).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }
}
