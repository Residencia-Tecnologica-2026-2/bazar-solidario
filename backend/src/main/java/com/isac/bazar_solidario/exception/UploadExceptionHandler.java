package com.isac.bazar_solidario.exception;

import com.isac.bazar_solidario.controller.UploadController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice(assignableTypes = UploadController.class)
public class UploadExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> erroDeUpload(ResponseStatusException exception) {
        int status = exception.getStatusCode().value();
        String mensagem = exception.getReason() == null ? "Não foi possível processar o arquivo." : exception.getReason();
        return ResponseEntity.status(status).body(Map.of("status", status, "erro", mensagem));
    }
}
