package com.isac.bazar_solidario.exception;
import com.isac.bazar_solidario.controller.PedidoController;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
@RestControllerAdvice(assignableTypes=PedidoController.class)
public class PedidoExceptionHandler {
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<?> regra(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("erro",e.getReason()==null?"Operação não permitida":e.getReason()));}
}
