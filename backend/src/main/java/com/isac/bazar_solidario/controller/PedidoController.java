package com.isac.bazar_solidario.controller;
import com.isac.bazar_solidario.dto.*;
import com.isac.bazar_solidario.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
@RestController @RequestMapping("/pedidos") @RequiredArgsConstructor
public class PedidoController {
 private final PedidoService service;
 private final UsuarioAutenticadoService usuarios;
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public PedidoResponse criar(@RequestHeader("Idempotency-Key") String chave,@Valid @RequestBody PedidoRequest req,Authentication auth) {return service.criar(usuarios.obter(auth),chave,req);}
 @GetMapping public PageResponse<PedidoResponse> listar(Authentication auth,Pageable page) {return service.listar(usuarios.obter(auth),page);}
 @PatchMapping("/{id}/{acao}") public PedidoResponse mudar(@PathVariable Long id,@PathVariable String acao,Authentication auth) {return service.mudar(id,acao,usuarios.obter(auth));}
}
