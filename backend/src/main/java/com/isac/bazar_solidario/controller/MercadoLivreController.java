package com.isac.bazar_solidario.controller;

import com.isac.bazar_solidario.service.*;
import com.isac.bazar_solidario.model.MercadoLivreVinculo;
import com.isac.bazar_solidario.model.MercadoLivrePublicacao;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import tools.jackson.databind.JsonNode;
import java.util.*;

@RestController
@RequestMapping("/integracoes/mercadolivre")
public class MercadoLivreController {
    private final MercadoLivreService service;
    private final UsuarioAutenticadoService usuarios;
    public MercadoLivreController(MercadoLivreService service,UsuarioAutenticadoService usuarios) {this.service=service;this.usuarios=usuarios;}
    @GetMapping("/situacao") public Map<String,Object> situacao(Authentication auth) {return service.situacao(usuarios.obter(auth));}
    @PostMapping("/conectar") public Map<String,String> conectar(Authentication auth) {return Map.of("url",service.iniciar(usuarios.obter(auth)));}
    @GetMapping("/callback") public ResponseEntity<String> callback(@RequestParam(required=false) String state,@RequestParam(required=false) String code) {
        service.concluir(state,code);
        return ResponseEntity.ok().header("Cache-Control","no-store").header("Referrer-Policy","no-referrer")
            .contentType(MediaType.TEXT_HTML).body("<!doctype html><html lang='pt-BR'><meta charset='utf-8'><title>ISAC — conta conectada</title><h1>Conta de teste conectada.</h1><p>Volte ao aplicativo e atualize a conexão. Você pode fechar esta aba.</p></html>");
    }
    @GetMapping("/itens") public JsonNode itens(Authentication auth) {return service.listar(usuarios.obter(auth));}
    @GetMapping("/itens/{itemId}") public JsonNode item(@PathVariable String itemId,Authentication auth) {return service.consultarItem(itemId,usuarios.obter(auth));}
    @GetMapping("/itens/{itemId}/frete") public JsonNode frete(@PathVariable String itemId,@RequestParam String cep,Authentication auth) {return service.frete(itemId,cep,usuarios.obter(auth));}
    public record Vincular(@NotNull @Positive Long anuncioId,@NotBlank @Pattern(regexp="MLB[0-9]{6,20}") String itemId) {}
    @PostMapping("/vinculos") public MercadoLivreVinculo vincular(@Valid @RequestBody Vincular req,Authentication auth) {return service.vincular(req.anuncioId(),req.itemId(),usuarios.obter(auth));}
    @GetMapping("/vinculos") public List<MercadoLivreVinculo> vinculos(Authentication auth) {return service.vinculos(usuarios.obter(auth));}
    @GetMapping("/categorias/{categoria}/atributos") public JsonNode atributos(@PathVariable String categoria,Authentication auth) {return service.atributos(categoria,usuarios.obter(auth));}
    public record Publicar(@NotNull @Positive Long anuncioId,@NotBlank @Pattern(regexp="MLB[0-9]{2,20}") String categoriaId,
        @NotBlank String tipoAnuncio,Map<String,String> atributos) {}
    @PostMapping("/publicacoes") public MercadoLivrePublicacao publicar(@Valid @RequestBody Publicar req,Authentication auth) {
        return service.publicar(req.anuncioId(),req.categoriaId(),req.tipoAnuncio(),req.atributos(),usuarios.obter(auth));
    }
}
