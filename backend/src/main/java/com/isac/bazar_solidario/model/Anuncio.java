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
@Table(name = "anuncios", uniqueConstraints={
    @UniqueConstraint(columnNames={"vendedor_id","codigo_peca"}),
    @UniqueConstraint(columnNames={"vendedor_id","chave_publicacao"})})
@Getter @Setter @NoArgsConstructor
public class Anuncio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="codigo_peca",length=40) private String codigoPeca;
    @Column(name="chave_publicacao",length=64) private String chavePublicacao;
    @Column(length=64) private String assinaturaPublicacao;

    @Version
    @Column(nullable=false, columnDefinition="bigint default 0")
    private Long versao=0L;

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
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @OneToMany(mappedBy = "anuncio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    @BatchSize(size = 50)
    private List<Imagem> imagens = new ArrayList<>();

    @PrePersist
    void aoCriar() {
        this.dataCriacao = LocalDateTime.now();
        if(this.codigoPeca==null)this.codigoPeca="PEC-"+java.util.UUID.randomUUID().toString().replace("-","").substring(0,16).toUpperCase(java.util.Locale.ROOT);
    }

    public void ativar() { this.status = StatusAnuncio.ATIVO; }
    public void desativar() { this.status = StatusAnuncio.INATIVO; }
    public void alterarEstoque(Integer novoEstoque) { this.estoque = novoEstoque; }

    public void adicionarImagem(Imagem imagem) {
        imagem.setAnuncio(this);
        this.imagens.add(imagem);
    }

    public boolean pertenceA(Long usuarioId) {
        return vendedor != null && vendedor.getId().equals(usuarioId);
    }
}
