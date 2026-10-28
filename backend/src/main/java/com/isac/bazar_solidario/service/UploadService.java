package com.isac.bazar_solidario.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class UploadService {
    private static final long TAMANHO_MAXIMO = 5L * 1024 * 1024;
    private static final long PIXELS_MAXIMOS = 10_000_000L;
    private static final Map<String, String> EXTENSOES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final Path pastaUploads;

    public UploadService(@Value("${app.uploads.directory:uploads}") String diretorioUploads) {
        this.pastaUploads = Paths.get(diretorioUploads).toAbsolutePath().normalize();
    }

    public String salvar(MultipartFile arquivo) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            throw erro(HttpStatus.BAD_REQUEST, "O arquivo não pode estar vazio.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw erro(HttpStatus.PAYLOAD_TOO_LARGE, "O arquivo deve ter no máximo 5 MB.");
        }

        String tipo = arquivo.getContentType();
        if (tipo == null) {
            throw erro(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Envie uma imagem JPG, PNG ou WebP.");
        }
        tipo = tipo.toLowerCase(Locale.ROOT);
        String extensao = EXTENSOES.get(tipo);
        if (extensao == null) {
            throw erro(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Envie uma imagem JPG, PNG ou WebP.");
        }

        byte[] conteudo = arquivo.getBytes();
        validarImagem(conteudo, tipo);

        Files.createDirectories(pastaUploads);
        String nomeArquivo = UUID.randomUUID() + extensao;
        Path destino = pastaUploads.resolve(nomeArquivo).normalize();
        if (!destino.startsWith(pastaUploads)) {
            throw new IllegalStateException("Caminho de upload inválido");
        }
        Files.write(destino, conteudo);
        return "/uploads/" + nomeArquivo;
    }

    private void validarImagem(byte[] conteudo, String tipo) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(conteudo))) {
            if (input == null) {
                throw erro(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "O conteúdo não é uma imagem válida.");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw erro(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "O conteúdo não é uma imagem válida.");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String formato = reader.getFormatName().toLowerCase(Locale.ROOT);
                boolean formatoCompativel = switch (tipo) {
                    case "image/jpeg" -> formato.equals("jpeg") || formato.equals("jpg");
                    case "image/png" -> formato.equals("png");
                    case "image/webp" -> formato.equals("webp");
                    default -> false;
                };
                if (!formatoCompativel) {
                    throw erro(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                            "O conteúdo do arquivo não corresponde ao tipo informado.");
                }

                int largura = reader.getWidth(0);
                int altura = reader.getHeight(0);
                if (largura <= 0 || altura <= 0 || (long) largura * altura > PIXELS_MAXIMOS) {
                    throw erro(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                            "A imagem deve ter dimensões válidas e até 10 megapixels.");
                }
                if (reader.read(0) == null) {
                    throw erro(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "O conteúdo não é uma imagem válida.");
                }
            } catch (ResponseStatusException ex) {
                throw ex;
            } catch (IOException | RuntimeException ex) {
                throw erro(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "O conteúdo não é uma imagem válida.");
            } finally {
                reader.dispose();
            }
        }
    }

    private ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}
