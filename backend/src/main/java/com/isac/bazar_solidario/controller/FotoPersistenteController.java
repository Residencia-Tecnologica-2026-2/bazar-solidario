package com.isac.bazar_solidario.controller;
import com.isac.bazar_solidario.repository.ArquivoFotoRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.concurrent.TimeUnit;

@RestController
@ConditionalOnProperty(name="app.uploads.storage",havingValue="database")
public class FotoPersistenteController {
    private final ArquivoFotoRepository fotos;
    public FotoPersistenteController(ArquivoFotoRepository fotos) {this.fotos=fotos;}
    @GetMapping("/uploads/{nome}")
    public ResponseEntity<byte[]> foto(@PathVariable String nome) {
        if(!nome.matches("[a-f0-9-]{36}\\.(jpg|png|webp)"))return ResponseEntity.notFound().build();
        return fotos.findById(nome).map(f->ResponseEntity.ok().contentType(MediaType.parseMediaType(f.getTipo()))
            .header("X-Content-Type-Options","nosniff").cacheControl(CacheControl.maxAge(1,TimeUnit.DAYS).cachePublic()).body(f.getConteudo()))
            .orElseGet(()->ResponseEntity.notFound().build());
    }
}
