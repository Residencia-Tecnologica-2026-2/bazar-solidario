package com.isac.bazar_solidario.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record PedidoRequest(@NotEmpty @Size(max=30) List<@NotNull @Valid Item> itens) {
 public record Item(@NotNull @Positive Long anuncioId,@NotNull @Min(1) @Max(99) Integer quantidade) {}
}
