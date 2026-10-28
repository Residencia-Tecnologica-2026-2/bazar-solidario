package com.isac.bazar_solidario.exception;
import com.isac.bazar_solidario.controller.LojaController;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestControllerAdvice(assignableTypes=LojaController.class)
public class LojaExceptionHandler {
 @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<Map<String,Object>> validacao(MethodArgumentNotValidException e){
  Map<String,String> campos=new LinkedHashMap<>(); for(FieldError x:e.getBindingResult().getFieldErrors()) campos.putIfAbsent(x.getField(),x.getDefaultMessage());
  Map<String,Object> corpo=new LinkedHashMap<>(); corpo.put("status",400);corpo.put("erro","Dados inválidos");corpo.put("campos",campos);return ResponseEntity.badRequest().body(corpo);
 }
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<Map<String,Object>> status(ResponseStatusException e){
  HttpStatus s=HttpStatus.valueOf(e.getStatusCode().value()); return ResponseEntity.status(s).body(Map.of("status",s.value(),"erro",e.getReason()==null?"Erro":e.getReason()));
 }
}