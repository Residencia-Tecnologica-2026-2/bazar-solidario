package com.isac.bazar_solidario.controller;

import com.isac.bazar_solidario.config.SecurityConfig;
import com.isac.bazar_solidario.dto.AuthResponse;
import com.isac.bazar_solidario.dto.UsuarioResponse;
import com.isac.bazar_solidario.model.Role;
import com.isac.bazar_solidario.model.Usuario;
import com.isac.bazar_solidario.security.JwtService;
import com.isac.bazar_solidario.service.AnuncioService;
import com.isac.bazar_solidario.service.CategoriaService;
import com.isac.bazar_solidario.service.UsuarioAutenticadoService;
import com.isac.bazar_solidario.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Usa o SecurityConfig REAL + filtro JWT real (só o banco/service é simulado). */
@WebMvcTest({AuthController.class, UsuarioController.class, LojaController.class, AnuncioController.class, CategoriaController.class})
@Import({SecurityConfig.class, JwtService.class})
@TestPropertySource(properties = {
        "jwt.secret=uma-chave-de-teste-com-mais-de-32-caracteres!!",
        "jwt.expiration-ms=60000"})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;

    @MockitoBean UsuarioService usuarioService;
    @MockitoBean com.isac.bazar_solidario.service.LojaService lojaService;
    @MockitoBean UsuarioAutenticadoService autenticadoService;
    @MockitoBean AnuncioService anuncioService;
    @MockitoBean CategoriaService categoriaService;

    @Test
    void cadastroEPublicoDevolveTokenEnaoVazaSenha() throws Exception {
        when(usuarioService.cadastrar(any())).thenReturn(AuthResponse.de("tok", 1000,
                new UsuarioResponse(1L, "Ana", "ana@x.com", "LOJA")));

        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@x.com\",\"senha\":\"123456\",\"whatsapp\":\"5581999999999\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("tok"))
                .andExpect(jsonPath("$.usuario.senha").doesNotExist());
    }

    @Test
    void cadastroInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"x\",\"senha\":\"1\",\"whatsapp\":\"1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    @Test
    void semTokenRotaProtegidaRetorna401() throws Exception {
        mockMvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/anuncios/meus")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoRetorna401() throws Exception {
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenValidoAcessaRotaProtegida() throws Exception {
        Usuario u = new Usuario(1L, "Ana", "ana@x.com", "hash", Role.LOJA);
        when(autenticadoService.obter(any())).thenReturn(u);
        String token = jwtService.gerarToken("ana@x.com", "LOJA");

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@x.com"));
    }

    @Test
    void listagemDeLojasPublicaFuncionaSemToken() throws Exception {
        when(lojaService.listar(any())).thenReturn(new com.isac.bazar_solidario.dto.PageResponse<>(java.util.List.of(), 0, 20, 0, 0, true, true));
        mockMvc.perform(get("/lojas")).andExpect(status().isOk());
    }

    @Test
    void perfilDaLojaExigeToken() throws Exception {
        mockMvc.perform(get("/lojas/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/lojas/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"whatsapp\":\"5581999999999\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void perfilDaPropriaLojaAceitaTokenLoja() throws Exception {
        when(lojaService.minhaLoja(any())).thenReturn(
                new com.isac.bazar_solidario.dto.LojaResponse(1L, "Ana", null, null, null, null, null, "5581999999999"));
        String token = jwtService.gerarToken("ana@x.com", "LOJA");
        mockMvc.perform(get("/lojas/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana"));
    }

    @Test
    void listagemPublicaFuncionaSemToken() throws Exception {
        when(categoriaService.listar()).thenReturn(java.util.List.of());
        mockMvc.perform(get("/categorias")).andExpect(status().isOk());
    }
}
