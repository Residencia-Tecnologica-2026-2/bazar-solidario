package com.isac.bazar_solidario.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Collectors;
import java.util.List;
import com.isac.bazar_solidario.dto.EncontroResponse.LojaResumoResponse;
import com.isac.bazar_solidario.model.Encontro;

public record EncontroResponse (
    Long id,
    String titulo,
    LocalDate data,
    LocalTime horario,
    String local,
    String descricao,
    String status,
    LojaResumoResponse criador,
    List<LojaResumoResponse> participantes
) {

    public record LojaResumoResponse(Long id, String nome, String logoUrl) {}

    public static EncontroResponse fromEntity(Encontro encontro) {
        var criadorResumo = new LojaResumoResponse(
            encontro.getCriador().getId(),
            encontro.getCriador().getNome(),
            encontro.getCriador().getLogoUrl()
   
        );

        var participantesResumo = encontro.getParticipantes().stream()
            .map(l -> new LojaResumoResponse(l.getId(), l.getNome(), l.getLogoUrl()))
            .collect(Collectors.toList());

        return new EncontroResponse(
            encontro.getId(),
            encontro.getTitulo(),
            encontro.getData(),
            encontro.getHorario(),
            encontro.getLocal(),
            encontro.getDescricao(),
            encontro.getStatus().name(),
            criadorResumo,
            participantesResumo
        );
    }
    
}
