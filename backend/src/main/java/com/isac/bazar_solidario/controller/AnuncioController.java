package com.isac.bazar_solidario.controller;

import com.isac.bazar_solidario.dto.AnuncioRequest;
import com.isac.bazar_solidario.dto.AnuncioResponse;
import com.isac.bazar_solidario.dto.PageResponse;
import com.isac.bazar_solidario.model.Loja;
import com.isac.bazar_solidario.service.AnuncioService;
import com.isac.bazar_solidario.service.LojaAutenticadaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/anuncios")
public class AnuncioController {

    private static final Set<String> CAMPOS_ORDENAVEIS = Set.of("preco", "dataCriacao", "titulo");
    private static final int TAMANHO_MAXIMO_PAGINA = 50;

    private final AnuncioService anuncioService;
    private final LojaAutenticadaService lojaAutenticada;

    public AnuncioController(AnuncioService anuncioService, LojaAutenticadaService lojaAutenticada) {
        this.anuncioService = anuncioService;
        this.lojaAutenticada = lojaAutenticada;
    }

    @GetMapping
    public PageResponse<AnuncioResponse> listar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long lojaId,
            @RequestParam(required = false) BigDecimal precoMin,
            @RequestParam(required = false) BigDecimal precoMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "dataCriacao") String ordenarPor,
            @RequestParam(defaultValue = "desc") String direcao) {
        return anuncioService.listarPublicos(titulo, categoriaId, lojaId, precoMin, precoMax,
                montarPageable(page, size, ordenarPor, direcao));
    }

    @GetMapping("/meus")
    public PageResponse<AnuncioResponse> meus(Authentication authentication,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "12") int size) {
        Loja loja = lojaAutenticada.obter(authentication);
        return anuncioService.listarDaLoja(loja, montarPageable(page, size, "dataCriacao", "desc"));
    }

    @GetMapping("/{id}")
    public AnuncioResponse buscar(@PathVariable Long id, Authentication authentication) {
        Loja lojaLogada = lojaAutenticada.obterOpcional(authentication).orElse(null);
        return anuncioService.buscarPorId(id, lojaLogada);
    }

    @PostMapping
    public ResponseEntity<AnuncioResponse> criar(@Valid @RequestBody AnuncioRequest req,
                                                 Authentication authentication) {
        AnuncioResponse criado = anuncioService.criar(req, lojaAutenticada.obter(authentication));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    public AnuncioResponse atualizar(@PathVariable Long id,
                                     @Valid @RequestBody AnuncioRequest req,
                                     Authentication authentication) {
        return anuncioService.atualizar(id, req, lojaAutenticada.obter(authentication));
    }

    @PatchMapping("/{id}/desativar")
    public AnuncioResponse desativar(@PathVariable Long id, Authentication authentication) {
        return anuncioService.desativar(id, lojaAutenticada.obter(authentication));
    }

    @PatchMapping("/{id}/ativar")
    public AnuncioResponse ativar(@PathVariable Long id, Authentication authentication) {
        return anuncioService.ativar(id, lojaAutenticada.obter(authentication));
    }

    @PatchMapping("/{id}/vendido")
    public AnuncioResponse marcarComoVendido(@PathVariable Long id, Authentication authentication) {
        return anuncioService.marcarComoVendido(id, lojaAutenticada.obter(authentication));
    }

    private Pageable montarPageable(int page, int size, String ordenarPor, String direcao) {
        String campo = CAMPOS_ORDENAVEIS.contains(ordenarPor) ? ordenarPor : "dataCriacao";
        Sort.Direction dir = "asc".equalsIgnoreCase(direcao) ? Sort.Direction.ASC : Sort.Direction.DESC;
        int tamanho = Math.min(Math.max(size, 1), TAMANHO_MAXIMO_PAGINA);
        return PageRequest.of(Math.max(page, 0), tamanho, Sort.by(dir, campo));
    }
}
