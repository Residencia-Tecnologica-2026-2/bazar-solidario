package com.isac.bazar_solidario.service;

import java.time.LocalDate;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.HashSet;
import com.isac.bazar_solidario.dto.EncontroRequest;
import com.isac.bazar_solidario.dto.EncontroResponse;
import com.isac.bazar_solidario.model.Encontro;
import com.isac.bazar_solidario.model.Loja;
import com.isac.bazar_solidario.repository.EncontroRepository;
import com.isac.bazar_solidario.repository.LojaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EncontroService {
    private final EncontroRepository encontroRepository;
    private final LojaRepository lojaRepository;
    private final LojaAutenticadaService lojaAutenticadaService;

    public EncontroService(EncontroRepository encontroRepository,
                           LojaRepository lojaRepository,
                           LojaAutenticadaService lojaAutenticadaService) {
        this.encontroRepository = encontroRepository;
        this.lojaRepository = lojaRepository;
        this.lojaAutenticadaService = lojaAutenticadaService;
    }

    @Transactional(readOnly = true)
    public List<EncontroResponse> listarProximosAtivos() {
        return encontroRepository.listarProximosAtivos(LocalDate.now(), Encontro.StatusEncontro.ATIVO)
                .stream()
                .map(EncontroResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EncontroResponse> listarProximos(int quantidade) {
        return listarProximosAtivos().stream().limit(quantidade).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EncontroResponse buscarPorId(Long id) {
        Encontro encontro = encontroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Encontro não encontrado"));
        return EncontroResponse.fromEntity(encontro);
    }

    @Transactional
    public EncontroResponse criar(EncontroRequest request, Authentication authentication) {
        Loja lojaLogada = lojaAutenticadaService.obter(authentication);

        Encontro encontro = new Encontro();
        encontro.setTitulo(request.titulo());
        encontro.setData(request.data());
        encontro.setHorario(request.horario());
        encontro.setLocal(request.local());
        encontro.setDescricao(request.descricao());
        encontro.setCriador(lojaLogada);

        // O criador entra automaticamente como participante
        encontro.getParticipantes().add(lojaLogada);

        // Adiciona outras lojas participantes se informadas
        if (request.lojaIds() != null && !request.lojaIds().isEmpty()) {
            encontro.getParticipantes().addAll(obterLojasParticipantes(request.lojaIds()));
        }

        Encontro salvo = encontroRepository.save(encontro);
        return EncontroResponse.fromEntity(salvo);
    }

    @Transactional
    public EncontroResponse atualizar(Long id, EncontroRequest request, Authentication authentication) {
        Encontro encontro = encontroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Encontro não encontrado"));

        Loja lojaLogada = lojaAutenticadaService.obter(authentication);
        validarCriador(encontro, lojaLogada);

        encontro.setTitulo(request.titulo());
        encontro.setData(request.data());
        encontro.setHorario(request.horario());
        encontro.setLocal(request.local());
        encontro.setDescricao(request.descricao());

        if (request.lojaIds() != null) {
            encontro.getParticipantes().clear();
            encontro.getParticipantes().add(lojaLogada); // Garante o criador
            encontro.getParticipantes().addAll(obterLojasParticipantes(request.lojaIds()));
        }

        return EncontroResponse.fromEntity(encontroRepository.save(encontro));
    }

    @Transactional
    public void cancelar(Long id, Authentication authentication) {
        Encontro encontro = encontroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Encontro não encontrado"));

        Loja lojaLogada = lojaAutenticadaService.obter(authentication);
        validarCriador(encontro, lojaLogada);

        encontro.setStatus(Encontro.StatusEncontro.CANCELADO);
        encontroRepository.save(encontro);
    }

    private void validarCriador(Encontro encontro, Loja lojaLogada) {
        if (!encontro.getCriador().getId().equals(lojaLogada.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas a loja criadora pode editar ou cancelar este encontro.");
        }
    }

    private List<Loja> obterLojasParticipantes(List<Long> ids) {
        List<Loja> lojas = lojaRepository.findAllById(new HashSet<>(ids));
        if (lojas.size() != new HashSet<>(ids).size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma ou mais lojas participantes não existem.");
        }
        return lojas;
    }
}
