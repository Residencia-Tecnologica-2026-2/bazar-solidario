package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.dto.CategoriaResponse;
import com.isac.bazar_solidario.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll(Sort.by("nome")).stream()
                .map(CategoriaResponse::de)
                .toList();
    }
}

