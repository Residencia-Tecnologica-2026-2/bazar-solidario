package com.isac.bazar_solidario.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UploadServiceTest {
    @TempDir Path pastaTemporaria;
    private UploadService service;

    @BeforeEach
    void preparar() {
        service = new UploadService(pastaTemporaria.toString());
    }

    @Test
    void salvaImagemPngComNomeGeradoEConteudoIntegro() throws Exception {
        byte[] png = pngUmPixel();
        String url = service.salvar(new MockMultipartFile("arquivo", "cliente.png", "image/png", png));

        assertTrue(url.matches("/uploads/[0-9a-f-]+\\.png"));
        Path salvo = pastaTemporaria.resolve(url.substring("/uploads/".length()));
        assertTrue(Files.exists(salvo));
        assertTrue(java.util.Arrays.equals(png, Files.readAllBytes(salvo)));
    }

    @Test
    void rejeitaArquivoVazio() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.salvar(new MockMultipartFile("arquivo", "vazio.png", "image/png", new byte[0])));
        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void rejeitaConteudoQueNaoCorrespondeAoMime() throws Exception {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.salvar(new MockMultipartFile("arquivo", "falso.jpg", "image/jpeg", pngUmPixel())));
        assertEquals(415, exception.getStatusCode().value());
    }

    @Test
    void rejeitaConteudoMalformadoMesmoComMimeDeImagem() {
        byte[] pngFalso = new byte[64];
        pngFalso[0] = (byte) 0x89;
        pngFalso[1] = 'P';
        pngFalso[2] = 'N';
        pngFalso[3] = 'G';
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.salvar(new MockMultipartFile("arquivo", "falso.png", "image/png", pngFalso)));
        assertEquals(415, exception.getStatusCode().value());
        try (var arquivos = Files.list(pastaTemporaria)) {
            assertFalse(arquivos.findAny().isPresent());
        }
    }

    @Test
    void rejeitaArquivoAcimaDeCincoMegabytes() {
        byte[] grande = new byte[5 * 1024 * 1024 + 1];
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.salvar(new MockMultipartFile("arquivo", "grande.jpg", "image/jpeg", grande)));
        assertEquals(413, exception.getStatusCode().value());
    }

    private byte[] pngUmPixel() throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
