package com.isac.bazar_solidario.controller;

import com.fasterxml.jackson.databind.*;
import com.isac.bazar_solidario.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.http.MediaType;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
@TestPropertySource(properties="spring.datasource.url=jdbc:h2:mem:pedidos_test;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000")
class PedidoIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired AnuncioRepository anuncios;
    @Autowired PedidoRepository pedidos;
    @Autowired CategoriaRepository categorias;
    final ObjectMapper json = new ObjectMapper();

    String conta(String role) throws Exception {
        String email = UUID.randomUUID() + "@test.local";
        var result = mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content("""
            {"nome":"Teste pedido","email":"%s","senha":"Senha2026!","tipo":"%s","whatsapp":"5581999999999"}
            """.formatted(email, role))).andExpect(status().isCreated()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    long anuncio(String token, String preco, int estoque) throws Exception {
        var result = mvc.perform(post("/anuncios").header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON).content("""
            {"titulo":"Peça teste","preco":%s,"estoque":%d,"categoriaId":%d}
            """.formatted(preco, estoque, categorias.findAll().get(0).getId())))
            .andExpect(status().isCreated()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    MockHttpServletRequestBuilder criar(String token, String chave, long... ids) {
        String itens = Arrays.stream(ids).mapToObj(id -> "{\"anuncioId\":" + id + ",\"quantidade\":1}")
            .collect(java.util.stream.Collectors.joining(","));
        return post("/pedidos").header("Authorization", "Bearer " + token).header("Idempotency-Key", chave)
            .contentType(MediaType.APPLICATION_JSON).content("{\"itens\":[" + itens + "]}");
    }
    String key() { return UUID.randomUUID().toString(); }

    @Test void precoDivisaoPersistenciaIdempotenciaECancelamento() throws Exception {
        String loja = conta("LOJA"), cliente = conta("CLIENTE");
        long a = anuncio(loja, "89.99", 2);
        String chave = key();
        var result = mvc.perform(criar(cliente, chave, a)).andExpect(status().isCreated())
            .andExpect(jsonPath("$.total").value(89.99)).andExpect(jsonPath("$.valorIsac").value(9.00))
            .andExpect(jsonPath("$.valorBrecho").value(80.99))
            .andExpect(jsonPath("$.estado").value("AGUARDANDO_CONFIRMACAO_MANUAL")).andReturn();
        long id = json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        assertEquals(1, anuncios.findById(a).orElseThrow().getEstoque());
        assertTrue(pedidos.existsById(id));
        mvc.perform(criar(cliente, chave, a)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(id));
        assertEquals(1, anuncios.findById(a).orElseThrow().getEstoque());
        mvc.perform(get("/pedidos").header("Authorization", "Bearer " + cliente))
            .andExpect(jsonPath("$.content[0].itens[0].titulo").value("Peça teste"));
        for (int i = 0; i < 2; i++) mvc.perform(patch("/pedidos/" + id + "/cancelar")
            .header("Authorization", "Bearer " + cliente)).andExpect(status().isOk());
        assertEquals(2, anuncios.findById(a).orElseThrow().getEstoque());
    }

    @Test void autorizacaoPorPapelEPropriedade() throws Exception {
        String loja = conta("LOJA"), outro = conta("LOJA"), cliente = conta("CLIENTE"), estranho = conta("CLIENTE");
        long a = anuncio(loja, "25.00", 1);
        mvc.perform(criar(loja, key(), a)).andExpect(status().isForbidden());
        mvc.perform(get("/pedidos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/pedidos").header("Authorization", "Bearer adulterado")).andExpect(status().isUnauthorized());
        mvc.perform(get("/anuncios/meus").header("Authorization", "Bearer " + cliente)).andExpect(status().isForbidden());
        var result = mvc.perform(criar(cliente, key(), a)).andExpect(status().isCreated()).andReturn();
        long id = json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        for (String t : List.of(outro, estranho, cliente)) mvc.perform(patch("/pedidos/" + id + "/confirmar")
            .header("Authorization", "Bearer " + t)).andExpect(status().isForbidden());
        mvc.perform(patch("/pedidos/" + id + "/cancelar").header("Authorization", "Bearer " + estranho)).andExpect(status().isForbidden());
        mvc.perform(get("/pedidos").header("Authorization", "Bearer " + outro)).andExpect(jsonPath("$.content").isEmpty());
        mvc.perform(patch("/pedidos/" + id + "/confirmar").header("Authorization", "Bearer " + loja))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CONFIRMADO"));
        mvc.perform(patch("/pedidos/" + id + "/cancelar").header("Authorization", "Bearer " + cliente)).andExpect(status().isConflict());
        mvc.perform(patch("/pedidos/" + id + "/cancelar").header("Authorization", "Bearer " + loja)).andExpect(status().isOk());
        assertEquals(1, anuncios.findById(a).orElseThrow().getEstoque());
    }

    @Test void falhaReverteTodaReservaERecusaMisturaDeLojas() throws Exception {
        String loja = conta("LOJA"), outra = conta("LOJA"), cliente = conta("CLIENTE");
        long a = anuncio(loja, "10.00", 1), b = anuncio(outra, "20.00", 1), c = anuncio(loja, "30.00", 0);
        mvc.perform(criar(cliente, key(), a, b)).andExpect(status().isBadRequest());
        assertEquals(1, anuncios.findById(a).orElseThrow().getEstoque());
        mvc.perform(criar(cliente, key(), a, c)).andExpect(status().isConflict());
        assertEquals(1, anuncios.findById(a).orElseThrow().getEstoque());
        mvc.perform(criar(cliente, key(), a, a)).andExpect(status().isBadRequest());
    }

    @Test void duasComprasSimultaneasNaoVendemAMesmaUnidade() throws Exception {
        String loja = conta("LOJA"), c1 = conta("CLIENTE"), c2 = conta("CLIENTE");
        long a = anuncio(loja, "11.11", 1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var start = new CountDownLatch(1);
            List<Future<Integer>> resultados = new ArrayList<>();
            for (String c : List.of(c1, c2)) resultados.add(executor.submit(() -> {
                start.await(); return mvc.perform(criar(c, key(), a)).andReturn().getResponse().getStatus();
            }));
            start.countDown();
            var codigos = new ArrayList<Integer>();
            for (var f : resultados) codigos.add(f.get(15, TimeUnit.SECONDS));
            Collections.sort(codigos);
            assertEquals(List.of(201, 409), codigos);
            assertEquals(0, anuncios.findById(a).orElseThrow().getEstoque());
        }
    }

    @Test void precoEnviadoPeloNavegadorNaoSubstituiOPrecoDoBanco() throws Exception {
        String loja = conta("LOJA"), cliente = conta("CLIENTE");
        long a = anuncio(loja, "75.50", 1);
        mvc.perform(post("/pedidos").header("Authorization", "Bearer " + cliente)
            .header("Idempotency-Key", key()).contentType(MediaType.APPLICATION_JSON).content("""
            {"total":0.01,"valorIsac":0,"itens":[{"anuncioId":%d,"quantidade":1,"preco":0.01}]}
            """.formatted(a))).andExpect(status().isCreated()).andExpect(jsonPath("$.total").value(75.50))
            .andExpect(jsonPath("$.valorIsac").value(7.55));
    }

    @Test void edicaoAntigaNaoPodeReporUnidadeQueAcabouDeSerReservada() throws Exception {
        String loja = conta("LOJA"), cliente = conta("CLIENTE");
        long a = anuncio(loja, "30.00", 1);
        long versao = anuncios.findById(a).orElseThrow().getVersao();
        mvc.perform(criar(cliente, key(), a)).andExpect(status().isCreated());
        mvc.perform(put("/anuncios/" + a).header("Authorization", "Bearer " + loja)
            .contentType(MediaType.APPLICATION_JSON).content("""
            {"titulo":"Edição antiga","preco":30,"estoque":1,"categoriaId":%d,"versao":%d}
            """.formatted(categorias.findAll().get(0).getId(), versao))).andExpect(status().isConflict());
        assertEquals(0, anuncios.findById(a).orElseThrow().getEstoque());
    }

    @Test void repeticaoSimultaneaDoMesmoPedidoReservaUmaVez() throws Exception {
        String loja = conta("LOJA"), cliente = conta("CLIENTE"), chave = key();
        long a = anuncio(loja, "10.00", 2);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var start = new CountDownLatch(1);
            List<Future<Long>> resultados = new ArrayList<>();
            for (int i = 0; i < 2; i++) resultados.add(executor.submit(() -> {
                start.await();
                var r = mvc.perform(criar(cliente, chave, a)).andExpect(status().isCreated()).andReturn();
                return json.readTree(r.getResponse().getContentAsString()).get("id").asLong();
            }));
            start.countDown();
            assertEquals(resultados.get(0).get(15, TimeUnit.SECONDS), resultados.get(1).get(15, TimeUnit.SECONDS));
            assertEquals(1, anuncios.findById(a).orElseThrow().getEstoque());
        }
    }
}
