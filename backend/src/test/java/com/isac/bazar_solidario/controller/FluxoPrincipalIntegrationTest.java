package com.isac.bazar_solidario.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isac.bazar_solidario.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FluxoPrincipalIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CategoriaRepository categoriaRepository;

    @Test
    void cadastroLoginAnunciosEncontrosHomeEFotosFuncionamIntegrados() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encontros").isArray())
                .andExpect(jsonPath("$.lojas").isArray());
        mockMvc.perform(get("/anuncios")).andExpect(status().isOk());
        mockMvc.perform(get("/anuncios/meus")).andExpect(status().isUnauthorized());

        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Brechó Teste","email":"teste@bazar.local",
                                 "senha":"senha123","whatsapp":"5581999999999"}
                                """))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""{"email":"teste@bazar.local","senha":"senha123"}"""))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode loginJson = objectMapper.readTree(login.getResponse().getContentAsString());
        String token = loginJson.get("token").asText();

        MvcResult loja = mockMvc.perform(get("/lojas/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        long lojaId = objectMapper.readTree(loja.getResponse().getContentAsString()).get("id").asLong();

        long categoriaId = categoriaRepository.findAll().stream()
                .findFirst().orElseThrow().getId();
        mockMvc.perform(post("/anuncios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Casaco de teste","descricao":"","preco":25.00,
                                 "estoque":1,"categoriaId":%d,"imagens":["/uploads/foto.png"]}
                                """.formatted(categoriaId)))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/anuncios").param("lojaId", Long.toString(lojaId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Casaco de teste"));

        mockMvc.perform(post("/encontros")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Feira de teste","data":"%s","horario":"14:00",
                                 "local":"Praça de teste","lojaIds":[]}
                                """.formatted(LocalDate.now().plusDays(1))))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encontros", hasSize(1)))
                .andExpect(jsonPath("$.lojas", hasSize(1)))
                .andExpect(jsonPath("$.lojas[0].endereco").hasJsonPath())
                .andExpect(jsonPath("$.lojas[0].latitude").hasJsonPath());

        MvcResult upload = mockMvc.perform(multipart("/uploads")
                        .file(new MockMultipartFile("arquivo", "foto.png", "image/png", pngUmPixel()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(org.hamcrest.Matchers.matchesRegex("/uploads/[0-9a-f-]+\\.png")))
                .andReturn();
        String url = objectMapper.readTree(upload.getResponse().getContentAsString()).get("url").asText();
        mockMvc.perform(get(url)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("image/png"));
    }

    @Test
    void uploadRecusaRequisicaoSemAutenticacao() throws Exception {
        mockMvc.perform(multipart("/uploads")
                        .file(new MockMultipartFile("arquivo", "foto.png", "image/png", pngUmPixel())))
                .andExpect(status().isUnauthorized());
    }

    private byte[] pngUmPixel() throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
