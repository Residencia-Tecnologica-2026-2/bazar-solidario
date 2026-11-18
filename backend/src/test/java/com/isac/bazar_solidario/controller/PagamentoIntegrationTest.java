package com.isac.bazar_solidario.controller;

import tools.jackson.databind.*;
import com.isac.bazar_solidario.config.AsaasConfig;
import com.isac.bazar_solidario.repository.*;
import com.isac.bazar_solidario.service.AsaasGateway;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.util.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
@TestPropertySource(properties={"spring.datasource.url=jdbc:h2:mem:pagamentos_test;DB_CLOSE_DELAY=-1",
    "app.asaas.api-key=chave-ficticia-nao-utilizavel", "app.asaas.seller-id=900",
    "app.asaas.isac-wallet-id=00000000-0000-0000-0000-000000000001",
    "app.asaas.webhook-token=token-exclusivo-de-testes-sem-acesso-real"})
class PagamentoIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired AsaasConfig config;
    @Autowired CategoriaRepository categorias;
    @Autowired PagamentoRepository pagamentos;
    @Autowired AnuncioRepository anuncios;
    @MockitoBean AsaasGateway gateway;
    final ObjectMapper json=new ObjectMapper();
    record Conta(String token,long id){}
    record Cenario(Conta cliente,Conta loja,long pedido,long anuncio){}
    Conta conta(String tipo) throws Exception {
        var res=mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content("""
            {"nome":"Pessoa de teste","email":"%s@test.local","senha":"Senha2026!","tipo":"%s","whatsapp":"5581999999999"}
            """.formatted(UUID.randomUUID(),tipo))).andExpect(status().isCreated()).andReturn();
        var node=json.readTree(res.getResponse().getContentAsString());
        return new Conta(node.path("token").asText(),node.path("usuario").path("id").asLong());
    }
    Cenario cenario() throws Exception {
        var loja=conta("LOJA");var cliente=conta("CLIENTE");
        ReflectionTestUtils.setField(config,"sellerId",loja.id());
        var res=mvc.perform(post("/anuncios").header("Idempotency-Key",UUID.randomUUID().toString()).header("Authorization","Bearer "+loja.token())
            .contentType(MediaType.APPLICATION_JSON).content("""
            {"titulo":"Peça do teste","preco":99.95,"estoque":2,"categoriaId":%d}
            """.formatted(categorias.findAll().get(0).getId()))).andExpect(status().isCreated()).andReturn();
        long anuncio=json.readTree(res.getResponse().getContentAsString()).path("id").asLong();
        res=mvc.perform(post("/pedidos").header("Authorization","Bearer "+cliente.token())
            .header("Idempotency-Key",UUID.randomUUID().toString()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"itens\":[{\"anuncioId\":"+anuncio+",\"quantidade\":1}]}"))
            .andExpect(status().isCreated()).andReturn();
        return new Cenario(cliente,loja,json.readTree(res.getResponse().getContentAsString()).path("id").asLong(),anuncio);
    }
    void mockCobranca() {
        when(gateway.cobrar(anyString(),anyString(),anyString(),anyString(),any(),any())).thenAnswer(a->
            json.readTree("""
            {"id":"pay_teste_%s","externalReference":"%s","value":99.95,"netValue":97.95,
            "invoiceUrl":"https://sandbox.asaas.com/i/teste","status":"PENDING"}
            """.formatted(a.getArgument(0).toString().replace("-",""),a.getArgument(0).toString())));
    }
    void iniciar(Cenario c) throws Exception {
        mvc.perform(post("/pedidos/"+c.pedido()+"/pagamento").header("Authorization","Bearer "+c.cliente().token())
            .contentType(MediaType.APPLICATION_JSON).content("{\"cpf\":\"12345678909\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.ambiente").value("SANDBOX"));
    }
    @Test void divideValorServidorEImpedeCobrancaDuplicada() throws Exception {
        var c=cenario();mockCobranca();iniciar(c);iniciar(c);
        verify(gateway,times(1)).cobrar(anyString(),anyString(),anyString(),eq("12345678909"),eq(new BigDecimal("99.95")),eq(new BigDecimal("10.00")));
        var p=pagamentos.findByPedidoId(c.pedido()).orElseThrow();
        assertEquals("AGUARDANDO_PAGAMENTO",p.getEstado());
        assertEquals(new BigDecimal("87.95"),p.getLiquidoBrecho());
        mvc.perform(patch("/pedidos/"+c.pedido()+"/cancelar").header("Authorization","Bearer "+c.cliente().token()))
            .andExpect(status().isConflict());
        assertEquals(1,anuncios.findById(c.anuncio()).orElseThrow().getEstoque());
    }
    @Test void autenticaWebhookEConsultaPagamentoNoProvedor() throws Exception {
        var c=cenario();mockCobranca();iniciar(c);
        var p=pagamentos.findByPedidoId(c.pedido()).orElseThrow();
        when(gateway.consultar("/payments/"+p.getProvedorId())).thenReturn(json.readTree("""
            {"id":"%s","externalReference":"%s","value":99.95,"netValue":97.95,"status":"RECEIVED"}
            """.formatted(p.getProvedorId(),p.getReferencia())));
        var evento="{\"event\":\"PAYMENT_RECEIVED\",\"payment\":{\"id\":\""+p.getProvedorId()+"\"}}";
        mvc.perform(post("/webhooks/asaas").contentType(MediaType.APPLICATION_JSON).content(evento)).andExpect(status().isUnauthorized());
        for(int i=0;i<2;i++)mvc.perform(post("/webhooks/asaas").header("asaas-access-token","token-exclusivo-de-testes-sem-acesso-real")
            .contentType(MediaType.APPLICATION_JSON).content(evento)).andExpect(status().isOk());
        assertEquals("PAGO",pagamentos.findByPedidoId(c.pedido()).orElseThrow().getEstado());
        assertEquals(1,anuncios.findById(c.anuncio()).orElseThrow().getEstoque());
    }
    @Test void timeoutNaoCriaSegundaCobrancaEPermiteRecuperarPorReferencia() throws Exception {
        var c=cenario();when(gateway.cobrar(anyString(),anyString(),anyString(),anyString(),any(),any()))
            .thenThrow(new IllegalStateException("Resposta inconclusiva do provedor."));
        iniciar(c);iniciar(c);
        verify(gateway,times(1)).cobrar(anyString(),anyString(),anyString(),anyString(),any(),any());
        var p=pagamentos.findByPedidoId(c.pedido()).orElseThrow();assertEquals("RECONCILIAR",p.getEstado());
        when(gateway.consultar("/payments?externalReference="+p.getReferencia())).thenReturn(json.readTree("""
            {"data":[{"id":"pay_recuperado-2026","externalReference":"%s","value":99.95,"status":"PENDING"}]}
            """.formatted(p.getReferencia())));
        mvc.perform(get("/pedidos/"+c.pedido()+"/pagamento").header("Authorization","Bearer "+c.cliente().token()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("AGUARDANDO_PAGAMENTO"));
    }
    @Test void bloqueiaOutroClienteCpfInvalidoELojaNaoVinculada() throws Exception {
        var c=cenario();var estranho=conta("CLIENTE");
        mvc.perform(post("/pedidos/"+c.pedido()+"/pagamento").header("Authorization","Bearer "+estranho.token())
            .contentType(MediaType.APPLICATION_JSON).content("{\"cpf\":\"12345678909\"}")).andExpect(status().isForbidden());
        mvc.perform(post("/pedidos/"+c.pedido()+"/pagamento").header("Authorization","Bearer "+c.cliente().token())
            .contentType(MediaType.APPLICATION_JSON).content("{\"cpf\":\"11111111111\"}")).andExpect(status().isBadRequest());
        ReflectionTestUtils.setField(config,"sellerId",900L);
        mvc.perform(post("/pedidos/"+c.pedido()+"/pagamento").header("Authorization","Bearer "+c.cliente().token())
            .contentType(MediaType.APPLICATION_JSON).content("{\"cpf\":\"12345678909\"}")).andExpect(status().isConflict());
        verifyNoInteractions(gateway);
    }
    @Test void naoAceitaValorDivergenteDoServidor() throws Exception {
        var c=cenario();when(gateway.cobrar(anyString(),anyString(),anyString(),anyString(),any(),any()))
            .thenAnswer(a->json.readTree("{\"id\":\"pay_falso\",\"externalReference\":\""+a.getArgument(0)+"\",\"value\":1,\"status\":\"RECEIVED\"}"));
        mvc.perform(post("/pedidos/"+c.pedido()+"/pagamento").header("Authorization","Bearer "+c.cliente().token())
            .contentType(MediaType.APPLICATION_JSON).content("{\"cpf\":\"12345678909\"}")).andExpect(status().isConflict());
        assertEquals("RECONCILIAR",pagamentos.findByPedidoId(c.pedido()).orElseThrow().getEstado());
    }
    @Test void webhookRecusaIdentificadorQueAlteraOCaminhoDaConsulta() throws Exception {
        mvc.perform(post("/webhooks/asaas").header("asaas-access-token","token-exclusivo-de-testes-sem-acesso-real")
            .contentType(MediaType.APPLICATION_JSON).content("{\"payment\":{\"id\":\"pay_../outro-recurso\"}}"))
            .andExpect(status().isOk());
        verifyNoInteractions(gateway);
    }
}
