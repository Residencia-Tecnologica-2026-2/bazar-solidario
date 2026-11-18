package com.isac.bazar_solidario.controller;

import tools.jackson.databind.JsonNode;
import com.isac.bazar_solidario.config.AsaasConfig;
import com.isac.bazar_solidario.dto.PagamentoResponse;
import com.isac.bazar_solidario.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController @RequiredArgsConstructor
public class PagamentoController {
    private final PagamentoService service;
    private final UsuarioAutenticadoService usuarios;
    private final AsaasConfig config;
    public record Solicitar(@NotBlank @Size(max=18) String cpf){}
    @GetMapping("/pagamentos/configuracao") public Map<String,Object> configuracao(){
        return Map.of("habilitado",config.configurado(),"ambiente","SANDBOX","metodo","PIX");
    }
    @PostMapping("/pedidos/{id}/pagamento") public PagamentoResponse iniciar(@PathVariable Long id,@Valid @RequestBody Solicitar req,Authentication auth){
        return service.iniciar(id,req.cpf(),usuarios.obter(auth));
    }
    @GetMapping("/pedidos/{id}/pagamento") public PagamentoResponse consultar(@PathVariable Long id,Authentication auth){
        return service.consultar(id,usuarios.obter(auth));
    }
    @PostMapping("/webhooks/asaas") public Map<String,String> webhook(
        @RequestHeader(value="asaas-access-token",required=false) String token,@RequestBody JsonNode evento){
        if(config.getWebhookToken().length()<32 || token==null || !MessageDigest.isEqual(
            config.getWebhookToken().getBytes(StandardCharsets.UTF_8),token.getBytes(StandardCharsets.UTF_8)))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Webhook não autorizado.");
        service.webhook(evento.path("payment").path("id").asText());
        return Map.of("status","recebido");
    }
}
