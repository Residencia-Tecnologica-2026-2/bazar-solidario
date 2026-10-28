package com.isac.bazar_solidario.controller;

import com.isac.bazar_solidario.dto.AnuncioResponse;
import com.isac.bazar_solidario.dto.CategoriaResponse;
import com.isac.bazar_solidario.dto.PageResponse;
import com.isac.bazar_solidario.dto.VendedorResponse;
import com.isac.bazar_solidario.model.Usuario;
import com.isac.bazar_solidario.model.enums.StatusAnuncio;
import com.isac.bazar_solidario.service.AnuncioService;
import com.isac.bazar_solidario.service.CategoriaService;
import com.isac.bazar_solidario.service.UsuarioAutenticadoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AnuncioController.class, CategoriaController.class})
@Import(TestSecurityConfig.class)
class AnuncioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnuncioService anuncioService;
    @MockitoBean
    private CategoriaService categoriaService;
    @MockitoBean
    private UsuarioAutenticadoService usuarioAutenticado;

    private Usuario usuario;

    @BeforeEach
    void preparar() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Dona Maria");
        usuario.setEmail("maria@teste.com");
        when(usuarioAutenticado.obter(any())).thenReturn(usuario);
    }

    private static final String JSON_VALIDO = """
            {"titulo":"Camiseta","descricao":"Algodão","preco":25.00,"estoque":3,"categoriaId":10,
             "imagens":["https://x.com/a.jpg"]}""";

    // ---------- públicos ----------

    @Test
    void listagemPublicaNaoExigeLogin() throws Exception {
        when(anuncioService.listarPublicos(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(resposta()), 0, 12, 1, 1, true, true));

        mockMvc.perform(get("/anuncios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Camiseta"))
                .andExpect(jsonPath("$.totalElements").value(1))
                // nunca expõe dados sensíveis do vendedor
                .andExpect(jsonPath("$.content[0].vendedor.senha").doesNotExist())
                .andExpect(jsonPath("$.content[0].vendedor.email").doesNotExist());
    }

    @Test
    void filtrosDaListagemChegamAoService() throws Exception {
        when(anuncioService.listarPublicos(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 12, 0, 0, true, true));

        mockMvc.perform(get("/anuncios")
                        .param("titulo", "cami")
                        .param("categoriaId", "10")
                        .param("precoMin", "5")
                        .param("precoMax", "50.5"))
                .andExpect(status().isOk());

        verify(anuncioService).listarPublicos(eq("cami"), eq(10L),
                eq(new BigDecimal("5")), eq(new BigDecimal("50.5")), any(Pageable.class));
    }

    @Test
    void ordenacaoSoAceitaCamposPermitidosETamanhoELimitado() throws Exception {
        when(anuncioService.listarPublicos(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 50, 0, 0, true, true));

        mockMvc.perform(get("/anuncios")
                        .param("ordenarPor", "vendedor.senha")
                        .param("size", "9999"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(anuncioService).listarPublicos(any(), any(), any(), any(), captor.capture());
        assertEquals(50, captor.getValue().getPageSize());
        assertEquals("dataCriacao", captor.getValue().getSort().iterator().next().getProperty());
    }

    @Test
    void ordenacaoPorPrecoAscendente() throws Exception {
        when(anuncioService.listarPublicos(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 12, 0, 0, true, true));

        mockMvc.perform(get("/anuncios").param("ordenarPor", "preco").param("direcao", "asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(anuncioService).listarPublicos(any(), any(), any(), any(), captor.capture());
        var ordem = captor.getValue().getSort().iterator().next();
        assertEquals("preco", ordem.getProperty());
        assertEquals("ASC", ordem.getDirection().name());
    }

    @Test
    void consultaPorIdNaoExigeLogin() throws Exception {
        when(anuncioService.buscarPorId(eq(5L), any())).thenReturn(resposta());

        mockMvc.perform(get("/anuncios/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void anuncioInexistenteRetorna404() throws Exception {
        when(anuncioService.buscarPorId(eq(99L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Anúncio não encontrado"));

        mockMvc.perform(get("/anuncios/99")).andExpect(status().isNotFound());
    }

    @Test
    void categoriasSaoPublicas() throws Exception {
        when(categoriaService.listar()).thenReturn(List.of(new CategoriaResponse(10L, "Roupas", null)));

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Roupas"));
    }

    // ---------- autenticação ----------

    @Test
    void meusAnunciosExigeLogin() throws Exception {
        mockMvc.perform(get("/anuncios/meus")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "maria@teste.com")
    void meusAnunciosDoUsuarioAutenticado() throws Exception {
        when(anuncioService.listarDoUsuario(eq(usuario), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(resposta()), 0, 12, 1, 1, true, true));

        mockMvc.perform(get("/anuncios/meus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5));
    }

    @Test
    void criarSemLoginRetorna401() throws Exception {
        mockMvc.perform(post("/anuncios").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    // ---------- cadastro e validações ----------

    @Test
    @WithMockUser(username = "maria@teste.com")
    void criarAnuncioValidoRetorna201ComLocation() throws Exception {
        when(anuncioService.criar(any(), eq(usuario))).thenReturn(resposta());

        mockMvc.perform(post("/anuncios").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/anuncios/5")))
                .andExpect(jsonPath("$.titulo").value("Camiseta"));
    }

    @Test
    @WithMockUser
    void tituloObrigatorio() throws Exception {
        enviarInvalido("""
                {"titulo":"  ","preco":25.00,"estoque":3,"categoriaId":10}""", "titulo");
    }

    @Test
    @WithMockUser
    void precoDeveSerMaiorQueZero() throws Exception {
        enviarInvalido("""
                {"titulo":"Camiseta","preco":0,"estoque":3,"categoriaId":10}""", "preco");
    }

    @Test
    @WithMockUser
    void precoNegativoERejeitado() throws Exception {
        enviarInvalido("""
                {"titulo":"Camiseta","preco":-5,"estoque":3,"categoriaId":10}""", "preco");
    }

    @Test
    @WithMockUser
    void estoqueNaoPodeSerNegativo() throws Exception {
        enviarInvalido("""
                {"titulo":"Camiseta","preco":25,"estoque":-1,"categoriaId":10}""", "estoque");
    }

    @Test
    @WithMockUser
    void categoriaObrigatoria() throws Exception {
        enviarInvalido("""
                {"titulo":"Camiseta","preco":25,"estoque":3}""", "categoriaId");
    }

    @Test
    @WithMockUser
    void urlDeImagemInvalidaERejeitada() throws Exception {
        enviarInvalido("""
                {"titulo":"Camiseta","preco":25,"estoque":3,"categoriaId":10,"imagens":["javascript:alert(1)"]}""",
                "imagens[0]");
    }

    @Test
    @WithMockUser
    void estoqueZeroEPermitido() throws Exception {
        when(anuncioService.criar(any(), any())).thenReturn(resposta());

        mockMvc.perform(post("/anuncios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Camiseta","preco":25,"estoque":0,"categoriaId":10}"""))
                .andExpect(status().isCreated());
    }

    private void enviarInvalido(String json, String campo) throws Exception {
        mockMvc.perform(post("/anuncios").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos['" + campo + "']").exists());
    }

    // ---------- permissão de dono ----------

    @Test
    @WithMockUser
    void naoDonoRecebe403AoEditar() throws Exception {
        when(anuncioService.atualizar(eq(5L), any(), eq(usuario)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não é o proprietário deste anúncio"));

        mockMvc.perform(put("/anuncios/5").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void naoDonoRecebe403AoDesativar() throws Exception {
        when(anuncioService.desativar(5L, usuario))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não é o proprietário deste anúncio"));

        mockMvc.perform(patch("/anuncios/5/desativar")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void donoDesativaComSucesso() throws Exception {
        when(anuncioService.desativar(5L, usuario)).thenReturn(
                new AnuncioResponse(5L, "Camiseta", null, new BigDecimal("25.00"), 3, StatusAnuncio.INATIVO,
                        LocalDateTime.now(), new CategoriaResponse(10L, "Roupas", null),
                        new VendedorResponse(1L, "Dona Maria"), List.of()));

        mockMvc.perform(patch("/anuncios/5/desativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));
    }

    private AnuncioResponse resposta() {
        return new AnuncioResponse(5L, "Camiseta", "Algodão", new BigDecimal("25.00"), 3, StatusAnuncio.ATIVO,
                LocalDateTime.now(), new CategoriaResponse(10L, "Roupas", null),
                new VendedorResponse(1L, "Dona Maria"), List.of("https://x.com/a.jpg"));
    }
}
