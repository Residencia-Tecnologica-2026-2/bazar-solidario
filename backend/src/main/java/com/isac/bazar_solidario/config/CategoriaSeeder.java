package com.isac.bazar_solidario.config;

import com.isac.bazar_solidario.model.Categoria;
import com.isac.bazar_solidario.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cadastra categorias iniciais se ainda não existirem (idempotente).
 * Pode ser removida quando houver cadastro de categorias pelo ADMIN.
 */
@Component
@RequiredArgsConstructor
public class CategoriaSeeder implements CommandLineRunner {

    private static final List<String> CATEGORIAS = List.of(
            "Roupas", "Calçados", "Acessórios", "Livros",
            "Eletrônicos", "Casa e Decoração", "Brinquedos", "Outros");

    private final CategoriaRepository categoriaRepository;

    @Override
    public void run(String... args) {
        for (String nome : CATEGORIAS) {
            if (!categoriaRepository.existsByNomeIgnoreCase(nome)) {
                categoriaRepository.save(new Categoria(null, nome, null));
            }
        }
    }
}

