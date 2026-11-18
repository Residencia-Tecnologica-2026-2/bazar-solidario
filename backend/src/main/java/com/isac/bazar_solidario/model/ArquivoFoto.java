package com.isac.bazar_solidario.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Getter @Setter @Table(name="arquivos_foto")
public class ArquivoFoto {
    @Id @Column(length=50) private String nome;
    @Column(nullable=false,length=30) private String tipo;
    @JdbcTypeCode(SqlTypes.VARBINARY) @Column(nullable=false,length=5242880) private byte[] conteudo;
}
