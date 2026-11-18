package com.isac.bazar_solidario.exception;

import com.isac.bazar_solidario.controller.AnuncioController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Devolve as mensagens de validação campo a campo.
 * Restrito ao AnuncioController para não interferir no tratamento de erros dos outros módulos.
 */
@RestControllerAdvice(assignableTypes = AnuncioController.class)
public class AnuncioExceptionHandler {
    @ExceptionHandler(AnuncioSemelhanteException.class)
    public ResponseEntity<?> semelhantes(AnuncioSemelhanteException ex){return ResponseEntity.status(409).body(Map.of(
        "codigo","ANUNCIO_SEMELHANTE","erro",ex.getMessage(),"semelhantes",ex.getSugestoes()));}
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<?> regra(org.springframework.web.server.ResponseStatusException ex){return ResponseEntity.status(ex.getStatusCode()).body(Map.of("erro",ex.getReason()==null?"Operação não permitida.":ex.getReason()));}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("status", HttpStatus.BAD_REQUEST.value());
        corpo.put("erro", "Dados inválidos");
        corpo.put("campos", campos);
        return ResponseEntity.badRequest().body(corpo);
    }
}

