package com.isac.bazar_solidario.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Corpo de POST /anuncios e PUT /anuncios/{id}.
 * Não existe campo de vendedor: o dono é sempre o usuário autenticado.
 */
public record AnuncioRequest(

        @NotBlank(message = "O título é obrigatório")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
        String titulo,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String descricao,

        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "Preço inválido (máximo 8 inteiros e 2 decimais)")
        BigDecimal preco,

        @NotNull(message = "O estoque é obrigatório")
        @PositiveOrZero(message = "O estoque não pode ser negativo")
        Integer estoque,

        @NotNull(message = "A categoria é obrigatória")
        Long categoriaId,

        /** Só URLs por enquanto (upload real fica para a segunda etapa). Opcional. */
        @Size(max = 10, message = "No máximo 10 imagens por anúncio")
        List<
                @NotBlank(message = "A URL da imagem não pode ser vazia")
                @Size(max = 500, message = "A URL da imagem deve ter no máximo 500 caracteres")
                @Pattern(regexp = "^(https?://.+|/uploads/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp))$",
                        message = "A imagem deve usar uma URL HTTP(S) ou um caminho gerado por /uploads")
                String> imagens
) {
}
