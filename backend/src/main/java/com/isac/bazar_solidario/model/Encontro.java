package com.isac.bazar_solidario.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "encontro")
@Getter
@Setter
@NoArgsConstructor
public class Encontro {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String titulo;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime horario;

    @Column(name = "local_encontro", nullable = false, length = 255)
    private String local;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEncontro status = StatusEncontro.ATIVO;


    @ManyToOne
    @JoinColumn (name = "criador_id", nullable = false)
    private Loja criador;

    @ManyToMany
    @JoinTable (
        name = "encontro_lojas",
        joinColumns = @JoinColumn (name = "encontro_id"),
        inverseJoinColumns = @JoinColumn (name = "loja_id")
    )
    private Set<Loja> participantes = new HashSet<>();

    public enum StatusEncontro {
        ATIVO, CANCELADO
    }

}
