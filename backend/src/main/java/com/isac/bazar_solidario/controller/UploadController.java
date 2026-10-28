
package com.isac.bazar_solidario.controller;

import com.isac.bazar_solidario.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> enviar(
            @RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        String url = uploadService.salvar(arquivo);
        return Map.of("url", url);
    }
}
