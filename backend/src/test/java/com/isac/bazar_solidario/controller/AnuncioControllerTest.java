package com.isac.bazar_solidario.controller;

import com.isac.bazar_solidario.dto.AnuncioResponse;
import com.isac.bazar_solidario.dto.CategoriaResponse;
import com.isac.bazar_solidario.dto.LojaResumoResponse;
import com.isac.bazar_solidario.dto.PageResponse;
import com.isac.bazar_solidario.model.Loja;
import com.isac.bazar_solidario.model.enums.StatusAnuncio;
import com.isac.bazar_solidario.service.AnuncioService;
import com.isac.bazar_solidario.service.CategoriaService;
import com.isac.bazar_solidario.service.LojaAutenticadaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AnuncioController.class, CategoriaController.class})
@Import(TestSecurityConfig.class)
class AnuncioControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AnuncioService anuncioService;
    @MockitoBean CategoriaService categoriaService;
    @MockitoBean LojaAutenticadaService lojaAutenticada;

    private Loja loja;

    @BeforeEach
    void preparar() {
        loja = new Loja();
        loja.setId(1L); loja.setNome("Brechó da Ana"); loja.setWhatsapp("5581999999999");
        when(lojaAutenticada.obter(any())).thenReturn(loja);
    }

    private static final String JSON_VALIDO = """
            {"titulo":"Camiseta","descricao":"Algodão","preco":25.00,"estoque":3,"categoriaId":10,
             "imagens":["https://x.com/a.jpg"]}""";

    @Test
    void listagemPublicaNaoExigeLogin() throws Exception {
        when(anuncioService.listarPublicos(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(resposta()), 0, 12, 1, 1, true, true));
        mockMvc.perform(get("/anuncios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Camiseta"))
                .andExpect(jsonPath("$.content[0].loja.nome").value("Brechó da Ana"))
                .andExpect(jsonPath("$.content[0].whatsappLink").exists());
    }

    @Test
    void filtroLojaIdPassadoAoService() throws Exception {
        when(anuncioService.listarPublicos(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 12, 0, 0, true, true));
        mockMvc.perform(get("/anuncios").param("lojaId", "42")).andExpect(status().isOk());
        ArgumentCaptor<Long> cap = ArgumentCaptor.forClass(Long.class);
        verify(anuncioService).listarPublicos(any(), any(), cap.capture(), any(), any(), any(Pageable.class));
        assertEquals(42L, cap.getValue());
    }

    @Test
    void consultaPorIdNaoExigeLogin() throws Exception {
        when(anuncioService.buscarPorId(eq(5L), any())).thenReturn(resposta());
        mockMvc.perform(get("/anuncios/5")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void anuncioInexistenteRetorna404() throws Exception {
        when(anuncioService.buscarPorId(eq(99L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mockMvc.perform(get("/anuncios/99")).andExpect(status().isNotFound());
    }

    @Test
    void meusAnunciosExigeLogin() throws Exception {
        mockMvc.perform(get("/anuncios/meus")).andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser
    void meusAnunciosDaLojaAutenticada() throws Exception {
        when(anuncioService.listarDaLoja(eq(loja), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(resposta()), 0, 12, 1, 1, true, true));
        mockMvc.perform(get("/anuncios/meus")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5));
    }

    @Test
    void criarSemLoginRetorna401() throws Exception {
        mockMvc.perform(post("/anuncios").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser
    void criarAnuncioValidoRetorna201() throws Exception {
        when(anuncioService.criar(any(), eq(loja))).thenReturn(resposta());
        mockMvc.perform(post("/anuncios").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/anuncios/5")));
    }

    @Test @WithMockUser
    void semImagemRetorna400() throws Exception {
        mockMvc.perform(post("/anuncios").contentType(MediaType.APPLICATION_JSON).content(
                """
                {"titulo":"Camiseta","preco":25.00,"estoque":3,"categoriaId":10,"imagens":[]}"""))
                .andExpect(status().isBadRequest());
    }

    @Test @WithMockUser
    void imagemUploadPathEAceita() throws Exception {
        when(anuncioService.criar(any(), any())).thenReturn(resposta());
        mockMvc.perform(post("/anuncios").contentType(MediaType.APPLICATION_JSON).content(
                """
                {"titulo":"Camiseta","preco":25.00,"estoque":3,"categoriaId":10,
                 "imagens":["/uploads/foto.jpg"]}"""))
                .andExpect(status().isCreated());
    }

    @Test @WithMockUser
    void tituloObrigatorio() throws Exception {
        mockMvc.perform(post("/anuncios").contentType(MediaType.APPLICATION_JSON).content(
                """
                {"titulo":"  ","preco":25.00,"estoque":3,"categoriaId":10,"imagens":["https://x.com/a.jpg"]}"""))
                .andExpect(status().isBadRequest());
    }

    @Test @WithMockUser
    void urlDeImagemInvalidaERejeitada() throws Exception {
        mockMvc.perform(post("/anuncios").contentType(MediaType.APPLICATION_JSON).content(
                """
                {"titulo":"Camiseta","preco":25,"estoque":3,"categoriaId":10,"imagens":["javascript:xss"]}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void marcarComoVendidoSemLoginRetorna401() throws Exception {
        mockMvc.perform(patch("/anuncios/5/vendido")).andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser
    void donoPodeMarcaComoVendido() throws Exception {
        when(anuncioService.marcarComoVendido(5L, loja)).thenReturn(
                new AnuncioResponse(5L, "Camiseta", null, new BigDecimal("25.00"), 3, StatusAnuncio.VENDIDO,
                        LocalDateTime.now(), new CategoriaResponse(10L, "Roupas", null),
                        new LojaResumoResponse(1L, "Brechó da Ana", null), List.of(), null));
        mockMvc.perform(patch("/anuncios/5/vendido")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VENDIDO"));
    }

    @Test @WithMockUser
    void anuncioJaVendidoRetorna409() throws Exception {
        when(anuncioService.marcarComoVendido(5L, loja))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));
        mockMvc.perform(patch("/anuncios/5/vendido")).andExpect(status().isConflict());
    }

    private AnuncioResponse resposta() {
        return new AnuncioResponse(5L, "Camiseta", "Algodão", new BigDecimal("25.00"), 3, StatusAnuncio.ATIVO,
                LocalDateTime.now(), new CategoriaResponse(10L, "Roupas", null),
                new LojaResumoResponse(1L, "Brechó da Ana", null),
                List.of("https://x.com/a.jpg"), "https://wa.me/5581999999999?text=Ol%C3%A1");
    }
}
