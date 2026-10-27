package com.isac.bazar_solidario.model;

import com.isac.bazar_solidario.model.enums.StatusAnuncio;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "anuncios")
@Getter @Setter @NoArgsConstructor
public class Anuncio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 2000)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer estoque;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAnuncio status = StatusAnuncio.ATIVO;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loja_id")
    private Loja loja;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @OneToMany(mappedBy = "anuncio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    @BatchSize(size = 50)
    private List<Imagem> imagens = new ArrayList<>();

    @PrePersist
    void aoCriar() { this.dataCriacao = LocalDateTime.now(); }

    public void ativar() { this.status = StatusAnuncio.ATIVO; }
    public void desativar() { this.status = StatusAnuncio.INATIVO; }
    public void marcarComoVendido() { this.status = StatusAnuncio.VENDIDO; }
    public void alterarEstoque(Integer novoEstoque) { this.estoque = novoEstoque; }

    public void adicionarImagem(Imagem imagem) {
        imagem.setAnuncio(this);
        this.imagens.add(imagem);
    }

    public boolean pertenceA(Long lojaId) {
        return loja != null && loja.possuiId(lojaId);
    }
}
