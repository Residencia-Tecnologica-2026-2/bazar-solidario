package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.dto.AnuncioRequest;
import com.isac.bazar_solidario.dto.AnuncioResponse;
import com.isac.bazar_solidario.dto.PageResponse;
import com.isac.bazar_solidario.model.Anuncio;
import com.isac.bazar_solidario.model.Categoria;
import com.isac.bazar_solidario.model.Loja;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnuncioServiceTest {

    @Mock private AnuncioRepository anuncioRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @InjectMocks private AnuncioService service;

    private Loja donoLoja;
    private Loja outraLoja;
    private Categoria categoria;

    @BeforeEach
    void preparar() {
        donoLoja  = loja(1L, "Brechó da Ana", "5581999999999");
        outraLoja = loja(2L, "Outro Brechó", null);
        categoria = new Categoria(10L, "Roupas", null);
    }

    @Test
    void criarUsaALojaAutenticadaComoProprietaria() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria));
        when(anuncioRepository.save(any(Anuncio.class))).thenAnswer(i -> i.getArgument(0));
        AnuncioResponse resp = service.criar(request(List.of("https://x.com/a.jpg")), donoLoja);
        ArgumentCaptor<Anuncio> captor = ArgumentCaptor.forClass(Anuncio.class);
        verify(anuncioRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getLoja().getId());
        assertEquals(StatusAnuncio.ATIVO, captor.getValue().getStatus());
        assertEquals("Camiseta", resp.titulo());
    }

    @Test
    void criarComCategoriaInexistenteRetorna400() {
        when(categoriaRepository.findById(10L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.criar(request(List.of("https://x.com/a.jpg")), donoLoja));
        assertEquals(400, ex.getStatusCode().value());
        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void donoPodeAtualizar() {
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncioDe(donoLoja)));
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria));
        AnuncioRequest req = new AnuncioRequest("Novo título  ", "desc", new BigDecimal("99.90"), 7, 10L,
                List.of("https://x.com/nova.jpg"));
        AnuncioResponse resp = service.atualizar(5L, req, donoLoja);
        assertEquals("Novo título", resp.titulo());
        assertEquals(new BigDecimal("99.90"), resp.preco());
    }

    @Test
    void naoDonoNaoPodeAtualizar() {
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncioDe(donoLoja)));
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.atualizar(5L, request(null), outraLoja)).getStatusCode().value());
    }

    @Test
    void donoPodeDesativar() {
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncioDe(donoLoja)));
        assertEquals(StatusAnuncio.INATIVO, service.desativar(5L, donoLoja).status());
    }

    @Test
    void naoDonoNaoPodeDesativar() {
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncioDe(donoLoja)));
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.desativar(5L, outraLoja)).getStatusCode().value());
    }

    @Test
    void anuncioVendidoNaoPodeSerDesativado() {
        Anuncio a = anuncioDe(donoLoja);
        a.setStatus(StatusAnuncio.VENDIDO);
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(a));
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.desativar(5L, donoLoja)).getStatusCode().value());
    }

    @Test
    void donoPodeMarcaComoVendido() {
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncioDe(donoLoja)));
        assertEquals(StatusAnuncio.VENDIDO, service.marcarComoVendido(5L, donoLoja).status());
    }

    @Test
    void naoDonoNaoPodeMarcaComoVendido() {
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncioDe(donoLoja)));
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.marcarComoVendido(5L, outraLoja)).getStatusCode().value());
    }

    @Test
    void anuncioInativoNaoPodeMarcaComoVendido() {
        Anuncio a = anuncioDe(donoLoja);
        a.desativar();
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(a));
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.marcarComoVendido(5L, donoLoja)).getStatusCode().value());
    }

    @Test
    void anuncioInativoNaoApareceParaVisitanteNemParaOutraLoja() {
        Anuncio a = anuncioDe(donoLoja);
        a.desativar();
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(a));
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.buscarPorId(5L, null)).getStatusCode().value());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.buscarPorId(5L, outraLoja)).getStatusCode().value());
    }

    @Test
    void donoVeOProprioAnuncioInativo() {
        Anuncio a = anuncioDe(donoLoja);
        a.desativar();
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(a));
        assertEquals(StatusAnuncio.INATIVO, service.buscarPorId(5L, donoLoja).status());
    }

    @Test
    void listagemComFaixaDePrecoInvertidaRetorna400() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.listarPublicos(null, null, null,
                        new BigDecimal("100"), new BigDecimal("10"), PageRequest.of(0, 12)))
                .getStatusCode().value());
    }

    @Test
    void listagemPublicaDevolvePaginaMapeada() {
        Pageable p = PageRequest.of(0, 12);
        when(anuncioRepository.findAll(ArgumentMatchers.<Specification<Anuncio>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(anuncioDe(donoLoja)), p, 1));
        PageResponse<AnuncioResponse> pagina =
                service.listarPublicos("cami", 10L, null, new BigDecimal("1"), new BigDecimal("500"), p);
        assertEquals(1, pagina.totalElements());
        assertEquals("Camiseta", pagina.content().get(0).titulo());
    }

    @Test
    void listagemDaLojaBuscaPeloIdDaLoja() {
        Pageable p = PageRequest.of(0, 12);
        when(anuncioRepository.findByLojaId(1L, p))
                .thenReturn(new PageImpl<>(List.of(anuncioDe(donoLoja)), p, 1));
        PageResponse<AnuncioResponse> pagina = service.listarDaLoja(donoLoja, p);
        assertEquals(1, pagina.content().size());
        verify(anuncioRepository).findByLojaId(1L, p);
    }

    @Test
    void whatsappLinkEGeradoCorretamente() {
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncioDe(donoLoja)));
        AnuncioResponse resp = service.buscarPorId(5L, null);
        assertNotNull(resp.whatsappLink());
        assertTrue(resp.whatsappLink().startsWith("https://wa.me/5581999999999?text="));
    }

    @Test
    void whatsappLinkENullQuandoLojaSemNumero() {
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(anuncioDe(outraLoja)));
        assertNull(service.buscarPorId(5L, null).whatsappLink());
    }

    private static Loja loja(Long id, String nome, String whatsapp) {
        Loja l = new Loja();
        l.setId(id); l.setNome(nome); l.setWhatsapp(whatsapp);
        return l;
    }

    private AnuncioRequest request(List<String> imagens) {
        return new AnuncioRequest("Camiseta", "Camiseta de algodão", new BigDecimal("25.00"), 3, 10L, imagens);
    }

    private Anuncio anuncioDe(Loja loja) {
        Anuncio a = new Anuncio();
        a.setId(5L); a.setTitulo("Camiseta"); a.setDescricao("Camiseta de algodão");
        a.setPreco(new BigDecimal("25.00")); a.setEstoque(3);
        a.setLoja(loja); a.setCategoria(categoria);
        return a;
    }
}
