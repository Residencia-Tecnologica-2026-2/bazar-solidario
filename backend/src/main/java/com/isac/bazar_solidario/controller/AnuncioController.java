package com.isac.bazar_solidario.controller;

import com.isac.bazar_solidario.dto.AnuncioRequest;
import com.isac.bazar_solidario.dto.AnuncioResponse;
import com.isac.bazar_solidario.dto.PageResponse;
import com.isac.bazar_solidario.model.Usuario;
import com.isac.bazar_solidario.service.AnuncioService;
import com.isac.bazar_solidario.service.UsuarioAutenticadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AnuncioController {

    private static final Set<String> CAMPOS_ORDENAVEIS = Set.of("preco", "dataCriacao", "titulo");
    private static final int TAMANHO_MAXIMO_PAGINA = 50;

    private final AnuncioService anuncioService;
    private final UsuarioAutenticadoService usuarioAutenticado;

    /** Listagem pública: só anúncios ATIVOS. */
    @GetMapping
    public PageResponse<AnuncioResponse> listar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) BigDecimal precoMin,
            @RequestParam(required = false) BigDecimal precoMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "dataCriacao") String ordenarPor,
            @RequestParam(defaultValue = "desc") String direcao) {

        return anuncioService.listarPublicos(titulo, categoriaId, precoMin, precoMax,
                montarPageable(page, size, ordenarPor, direcao));
    }

    /** Anúncios do usuário autenticado (inclui inativos). */
    @GetMapping("/meus")
    public PageResponse<AnuncioResponse> meus(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Usuario usuario = usuarioAutenticado.obter(authentication);
        return anuncioService.listarDoUsuario(usuario, montarPageable(page, size, "dataCriacao", "desc"));
    }

    @GetMapping("/{id}")
    public AnuncioResponse buscar(@PathVariable Long id, Authentication authentication) {
        Usuario solicitante = usuarioAutenticado.obterOpcional(authentication).orElse(null);
        return anuncioService.buscarPorId(id, solicitante);
    }

    @PostMapping
    public ResponseEntity<AnuncioResponse> criar(@Valid @RequestBody AnuncioRequest req,
                                                 Authentication authentication) {
        AnuncioResponse criado = anuncioService.criar(req, usuarioAutenticado.obter(authentication));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    public AnuncioResponse atualizar(@PathVariable Long id,
                                     @Valid @RequestBody AnuncioRequest req,
                                     Authentication authentication) {
        return anuncioService.atualizar(id, req, usuarioAutenticado.obter(authentication));
    }

    /** "Desativar" = soft delete (status INATIVO). Não apaga a linha. */
    @PatchMapping("/{id}/desativar")
    public AnuncioResponse desativar(@PathVariable Long id, Authentication authentication) {
        return anuncioService.desativar(id, usuarioAutenticado.obter(authentication));
    }

    @PatchMapping("/{id}/ativar")
    public AnuncioResponse ativar(@PathVariable Long id, Authentication authentication) {
        return anuncioService.ativar(id, usuarioAutenticado.obter(authentication));
    }

    /** Só aceita campos conhecidos: impede ordenar por propriedade arbitrária (ex.: vendedor.senha). */
    private Pageable montarPageable(int page, int size, String ordenarPor, String direcao) {
        String campo = CAMPOS_ORDENAVEIS.contains(ordenarPor) ? ordenarPor : "dataCriacao";
        Sort.Direction dir = "asc".equalsIgnoreCase(direcao) ? Sort.Direction.ASC : Sort.Direction.DESC;
        int tamanho = Math.min(Math.max(size, 1), TAMANHO_MAXIMO_PAGINA);
        return PageRequest.of(Math.max(page, 0), tamanho, Sort.by(dir, campo));
    }
}

