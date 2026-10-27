package com.isac.bazar_solidario.dto;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EncontroRequest(
    @NotBlank(message = "O título é obrigatório")
    String titulo,

    @NotNull (message = "A data é obrigatória")
    @FutureOrPresent(message = "A data não pode ser no passado")
    LocalDate data,

    @NotNull(message = "O horário é obrigatório")
    LocalTime horario,

    @NotBlank(message = "O local é obrigatório")
    @Size(max = 255, message = "O local deve ter no máximo 255 caracteres")
    String local,

    String descricao,

    List<Long> lojaIds



) {}
