
package com.isac.bazar_solidario.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadService {

    private final Path pastaUploads = Paths.get("uploads")
            .toAbsolutePath()
            .normalize();

    public String salvar(MultipartFile arquivo) throws IOException {

        // 1. Verifica se o arquivo está vazio
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O arquivo não pode estar vazio.");
        }

        // 2. Verifica o tamanho máximo de 5 MB
        if (arquivo.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "O arquivo deve ter no máximo 5 MB.");
        }

        // 3. Verifica o tipo MIME informado
        String tipo = arquivo.getContentType();

        if (tipo == null || !Set.of(
                "image/jpeg",
                "image/png",
                "image/webp").contains(tipo.toLowerCase())) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Envie uma imagem JPG, PNG ou WebP.");
        }

        // 4. Verifica a assinatura do conteúdo
        byte[] conteudo = arquivo.getBytes();

        if (!ehImagemValida(conteudo, tipo)) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "O conteúdo do arquivo não corresponde a uma imagem válida.");
        }

        // 5. Cria a pasta de uploads, caso não exista
        Files.createDirectories(pastaUploads);

        // 6. Define a extensão pelo tipo validado
        String extensao = switch (tipo.toLowerCase()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Formato não permitido.");
        };

        // 7. Gera um nome único para o arquivo
        String nomeArquivo = UUID.randomUUID() + extensao;

        Path destino = pastaUploads.resolve(nomeArquivo).normalize();

        // 8. Salva o arquivo
        arquivo.transferTo(destino);

        // 9. Retorna a URL pública
        return "/uploads/" + nomeArquivo;
    }

    private boolean ehImagemValida(byte[] dados, String tipo) {

        if (tipo == null || dados.length < 12) {
            return false;
        }

        tipo = tipo.toLowerCase();

        if (tipo.equals("image/jpeg")) {
            return (dados[0] & 0xFF) == 0xFF
                    && (dados[1] & 0xFF) == 0xD8
                    && (dados[2] & 0xFF) == 0xFF;
        }

        if (tipo.equals("image/png")) {
            return (dados[0] & 0xFF) == 0x89
                    && dados[1] == 0x50
                    && dados[2] == 0x4E
                    && dados[3] == 0x47
                    && dados[4] == 0x0D
                    && dados[5] == 0x0A
                    && dados[6] == 0x1A
                    && dados[7] == 0x0A;
        }

        if (tipo.equals("image/webp")) {
            return dados[0] == 'R'
                    && dados[1] == 'I'
                    && dados[2] == 'F'
                    && dados[3] == 'F'
                    && dados[8] == 'W'
                    && dados[9] == 'E'
                    && dados[10] == 'B'
                    && dados[11] == 'P';
        }

        return false;
    }
}
