package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.dto.AnuncioRequest;
import com.isac.bazar_solidario.dto.AnuncioResponse;
import com.isac.bazar_solidario.dto.PageResponse;
import com.isac.bazar_solidario.model.Anuncio;
import com.isac.bazar_solidario.model.Categoria;
import com.isac.bazar_solidario.model.Usuario;
import com.isac.bazar_solidario.model.enums.StatusAnuncio;
import com.isac.bazar_solidario.repository.AnuncioRepository;
import com.isac.bazar_solidario.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnuncioServiceTest {

    @Mock
    private AnuncioRepository anuncioRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @InjectMocks
    private AnuncioService service;

    private Usuario dono;
    private Usuario outro;
    private Categoria categoria;

    @BeforeEach
    void preparar() {
        dono = usuario(1L, "Dona Maria");
        outro = usuario(2L, "Outro Usuário");
        categoria = new Categoria(10L, "Roupas", null);
    }

    // ---------- criar ----------

    @Test
    void criarUsaOUsuarioAutenticadoComoVendedor() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria));
        when(anuncioRepository.save(any(Anuncio.class))).thenAnswer(i -> i.getArgument(0));

        AnuncioResponse resp = service.criar(request(List.of("https://x.com/a.jpg", "https://x.com/b.jpg")), dono);

        ArgumentCaptor<Anuncio> captor = ArgumentCaptor.forClass(Anuncio.class);
        verify(anuncioRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getVendedor().getId());
        assertEquals(StatusAnuncio.ATIVO, captor.getValue().getStatus());
        assertEquals("Camiseta", resp.titulo());
        assertEquals(List.of("https://x.com/a.jpg", "https://x.com/b.jpg"), resp.imagens());
    }

    @Test
    void criarComCategoriaInexistenteRetorna400() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.criar(request(null), dono));

        assertEquals(400, ex.getStatusCode().value());
        verify(anuncioRepository, never()).save(any());
    }

    // ---------- editar / desativar (somente o dono) ----------

    @Test
    void donoPodeAtualizar() {
        Anuncio anuncio = anuncioDe(dono);
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncio));
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria));

        AnuncioRequest req = new AnuncioRequest("Novo título  ", "desc", new BigDecimal("99.90"), 7, 10L, null);
        AnuncioResponse resp = service.atualizar(5L, req, dono);

        assertEquals("Novo título", resp.titulo());
        assertEquals(new BigDecimal("99.90"), resp.preco());
        assertEquals(7, resp.estoque());
    }

    @Test
    void atualizarSemImagensMantemAsAtuais() {
        Anuncio anuncio = anuncioDe(dono);
        com.isac.bazar_solidario.model.Imagem img = new com.isac.bazar_solidario.model.Imagem();
        img.setUrl("https://x.com/antiga.jpg");
        anuncio.adicionarImagem(img);
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncio));
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria));

        AnuncioResponse resp = service.atualizar(5L, request(null), dono);

        assertEquals(List.of("https://x.com/antiga.jpg"), resp.imagens());
    }

    @Test
    void naoDonoNaoPodeAtualizar() {
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncioDe(dono)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizar(5L, request(null), outro));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void donoPodeDesativar() {
        Anuncio anuncio = anuncioDe(dono);
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncio));

        AnuncioResponse resp = service.desativar(5L, dono);

        assertEquals(StatusAnuncio.INATIVO, resp.status());
    }

    @Test
    void naoDonoNaoPodeDesativar() {
        Anuncio anuncio = anuncioDe(dono);
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncio));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.desativar(5L, outro));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals(StatusAnuncio.ATIVO, anuncio.getStatus());
    }

    @Test
    void desativarAnuncioInexistenteRetorna404() {
        when(anuncioRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.desativar(99L, dono));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void anuncioVendidoNaoPodeSerDesativado() {
        Anuncio anuncio = anuncioDe(dono);
        anuncio.setStatus(StatusAnuncio.VENDIDO);
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncio));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.desativar(5L, dono));

        assertEquals(409, ex.getStatusCode().value());
    }

    // ---------- consulta ----------

    @Test
    void anuncioInativoNaoApareceParaVisitanteNemParaOutroUsuario() {
        Anuncio anuncio = anuncioDe(dono);
        anuncio.desativar();
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncio));

        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.buscarPorId(5L, null)).getStatusCode().value());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.buscarPorId(5L, outro)).getStatusCode().value());
    }

    @Test
    void donoVeOProprioAnuncioInativo() {
        Anuncio anuncio = anuncioDe(dono);
        anuncio.desativar();
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncio));

        assertEquals(StatusAnuncio.INATIVO, service.buscarPorId(5L, dono).status());
    }

    @Test
    void visitanteVeAnuncioAtivo() {
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncioDe(dono)));

        assertEquals("Camiseta", service.buscarPorId(5L, null).titulo());
    }

    // ---------- listagem ----------

    @Test
    void listagemComFaixaDePrecoInvertidaRetorna400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.listarPublicos(null, null, new BigDecimal("100"), new BigDecimal("10"),
                        PageRequest.of(0, 12)));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void listagemPublicaDevolvePaginaMapeada() {
        Pageable pageable = PageRequest.of(0, 12);
        when(anuncioRepository.findAll(ArgumentMatchers.<Specification<Anuncio>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(anuncioDe(dono)), pageable, 1));

        PageResponse<AnuncioResponse> pagina =
                service.listarPublicos("cami", 10L, new BigDecimal("1"), new BigDecimal("500"), pageable);

        assertEquals(1, pagina.totalElements());
        assertEquals("Camiseta", pagina.content().get(0).titulo());
    }

    @Test
    void listagemDoUsuarioBuscaPeloIdDoDono() {
        Pageable pageable = PageRequest.of(0, 12);
        when(anuncioRepository.findByVendedorId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(anuncioDe(dono)), pageable, 1));

        PageResponse<AnuncioResponse> pagina = service.listarDoUsuario(dono, pageable);

        assertEquals(1, pagina.content().size());
        verify(anuncioRepository).findByVendedorId(1L, pageable);
    }

    // ---------- helpers ----------

    private static Usuario usuario(Long id, String nome) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setEmail(nome.toLowerCase().replace(" ", ".") + "@teste.com");
        return u;
    }

    private AnuncioRequest request(List<String> imagens) {
        return new AnuncioRequest("Camiseta", "Camiseta de algodão", new BigDecimal("25.00"), 3, 10L, imagens);
    }

    private Anuncio anuncioDe(Usuario vendedor) {
        Anuncio a = new Anuncio();
        a.setId(5L);
        a.setTitulo("Camiseta");
        a.setDescricao("Camiseta de algodão");
        a.setPreco(new BigDecimal("25.00"));
        a.setEstoque(3);
        a.setVendedor(vendedor);
        a.setCategoria(categoria);
        return a;
    }
}
