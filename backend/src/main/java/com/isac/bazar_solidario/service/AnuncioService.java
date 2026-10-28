package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.dto.AnuncioRequest;
import com.isac.bazar_solidario.dto.AnuncioResponse;
import com.isac.bazar_solidario.dto.PageResponse;
import com.isac.bazar_solidario.model.Anuncio;
import com.isac.bazar_solidario.model.Categoria;
import com.isac.bazar_solidario.model.Imagem;
import com.isac.bazar_solidario.model.Usuario;
import com.isac.bazar_solidario.model.enums.StatusAnuncio;
import com.isac.bazar_solidario.repository.AnuncioRepository;
import com.isac.bazar_solidario.repository.AnuncioSpecs;
import com.isac.bazar_solidario.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AnuncioService {

    private final AnuncioRepository anuncioRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional
    public AnuncioResponse criar(AnuncioRequest req, Usuario vendedor) {
        Anuncio anuncio = new Anuncio();
        anuncio.setVendedor(vendedor); // sempre o usuário autenticado, nunca vindo do frontend
        preencher(anuncio, req);
        return AnuncioResponse.de(anuncioRepository.save(anuncio));
    }

    /**
     * Consulta pública. Anúncio INATIVO só aparece para o próprio dono;
     * para os demais responde 404 (não revela que existe).
     *
     * @param solicitante usuário logado ou null se for visitante
     */
    @Transactional(readOnly = true)
    public AnuncioResponse buscarPorId(Long id, Usuario solicitante) {
        Anuncio anuncio = anuncioRepository.findById(id).orElseThrow(this::naoEncontrado);

        boolean inativo = anuncio.getStatus() == StatusAnuncio.INATIVO;
        boolean ehDono = solicitante != null && anuncio.pertenceA(solicitante.getId());
        if (inativo && !ehDono) {
            throw naoEncontrado();
        }
        return AnuncioResponse.de(anuncio);
    }

    @Transactional(readOnly = true)
    public PageResponse<AnuncioResponse> listarPublicos(String titulo, Long categoriaId,
                                                        BigDecimal precoMin, BigDecimal precoMax,
                                                        Pageable pageable) {
        if (precoMin != null && precoMax != null && precoMin.compareTo(precoMax) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "precoMin não pode ser maior que precoMax");
        }

        Specification<Anuncio> spec = AnuncioSpecs.comStatus(StatusAnuncio.ATIVO);
        if (titulo != null && !titulo.isBlank()) {
            spec = spec.and(AnuncioSpecs.tituloContem(titulo.trim()));
        }
        if (categoriaId != null) {
            spec = spec.and(AnuncioSpecs.daCategoria(categoriaId));
        }
        if (precoMin != null) {
            spec = spec.and(AnuncioSpecs.precoMinimo(precoMin));
        }
        if (precoMax != null) {
            spec = spec.and(AnuncioSpecs.precoMaximo(precoMax));
        }

        return PageResponse.de(anuncioRepository.findAll(spec, pageable).map(AnuncioResponse::de));
    }

    /** Todos os anúncios do usuário, inclusive os INATIVOS. */
    @Transactional(readOnly = true)
    public PageResponse<AnuncioResponse> listarDoUsuario(Usuario usuario, Pageable pageable) {
        return PageResponse.de(
                anuncioRepository.findByVendedorId(usuario.getId(), pageable).map(AnuncioResponse::de));
    }

    @Transactional
    public AnuncioResponse atualizar(Long id, AnuncioRequest req, Usuario usuario) {
        Anuncio anuncio = buscarDoDono(id, usuario);
        if(req.versao()==null || !req.versao().equals(anuncio.getVersao())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"A peça mudou desde que você abriu o formulário. Recarregue antes de editar.");
        }
        preencher(anuncio, req);
        anuncioRepository.flush();
        return AnuncioResponse.de(anuncio);
    }

    @Transactional
    public AnuncioResponse desativar(Long id, Usuario usuario) {
        Anuncio anuncio = buscarDoDono(id, usuario);
        exigirNaoVendido(anuncio);
        anuncio.desativar();
        return AnuncioResponse.de(anuncio);
    }

    @Transactional
    public AnuncioResponse ativar(Long id, Usuario usuario) {
        Anuncio anuncio = buscarDoDono(id, usuario);
        exigirNaoVendido(anuncio);
        anuncio.ativar();
        return AnuncioResponse.de(anuncio);
    }

    // ---------- auxiliares ----------

    private Anuncio buscarDoDono(Long id, Usuario usuario) {
        Anuncio anuncio = anuncioRepository.bloquear(id).orElseThrow(this::naoEncontrado);
        if (!anuncio.pertenceA(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não é o proprietário deste anúncio");
        }
        return anuncio;
    }

    private void exigirNaoVendido(Anuncio anuncio) {
        if (anuncio.getStatus() == StatusAnuncio.VENDIDO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Anúncio já vendido não pode ter o status alterado");
        }
    }

    private void preencher(Anuncio anuncio, AnuncioRequest req) {
        Categoria categoria = categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Categoria inválida"));

        anuncio.setTitulo(req.titulo().trim());
        anuncio.setDescricao(req.descricao());
        anuncio.setPreco(req.preco());
        anuncio.alterarEstoque(req.estoque());
        anuncio.setCategoria(categoria);

        // null = mantém as imagens atuais; lista (mesmo vazia) = substitui
        if (req.imagens() != null) {
            anuncio.getImagens().clear();
            int ordem = 0;
            for (String url : req.imagens()) {
                Imagem imagem = new Imagem();
                imagem.setUrl(url.trim());
                imagem.setOrdem(ordem++);
                anuncio.adicionarImagem(imagem);
            }
        }
    }

    private ResponseStatusException naoEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Anúncio não encontrado");
    }
}
