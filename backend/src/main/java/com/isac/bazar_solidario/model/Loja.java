package com.isac.bazar_solidario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table (name = "lojas")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 




public class Loja {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false, length = 100)
    private String nome;

    private String logoUrl;

    @Column(length = 1000)
    private String descricao;

    private String endereco;

    @Column(nullable = false)
    private String whatsapp;

    @OneToOne 
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    

    
}
