package com.isac.bazar_solidario.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record AnuncioRequest(

        @NotBlank(message = "O título é obrigatório")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
        String titulo,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String descricao,

        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "Preço inválido")
        BigDecimal preco,

        @NotNull(message = "O estoque é obrigatório")
        @PositiveOrZero(message = "O estoque não pode ser negativo")
        Integer estoque,

        @NotNull(message = "A categoria é obrigatória")
        Long categoriaId,

        @NotNull(message = "Informe pelo menos 1 imagem")
        @Size(min = 1, max = 10, message = "Informe entre 1 e 10 imagens por anúncio")
        List<
                @NotBlank(message = "A URL da imagem não pode ser vazia")
                @Size(max = 500, message = "A URL da imagem deve ter no máximo 500 caracteres")
                @Pattern(regexp = "^(https?://.+|/uploads/.+)", message = "A URL da imagem deve começar com http://, https:// ou /uploads/")
                String> imagens
) {
}
