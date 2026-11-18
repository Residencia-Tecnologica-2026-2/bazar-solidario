package com.isac.bazar_solidario.service;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MercadoLivreGateway {
    private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper json=new ObjectMapper();
    public JsonNode token(Map<String,String> dados) {
        var form=dados.entrySet().stream().map(e->encode(e.getKey())+"="+encode(e.getValue())).collect(Collectors.joining("&"));
        return enviar(HttpRequest.newBuilder(URI.create("https://api.mercadolibre.com/oauth/token"))
            .header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form)));
    }
    public JsonNode get(String caminho,String token) {
        if(!caminho.startsWith("/") || caminho.startsWith("//") || caminho.contains(".."))throw new IllegalArgumentException("Recurso inválido.");
        return enviar(HttpRequest.newBuilder(URI.create("https://api.mercadolibre.com"+caminho))
            .header("Authorization","Bearer "+token).GET());
    }
    public JsonNode post(String caminho,String token,Object dados) {
        if(!java.util.Set.of("/items", "/items/validate").contains(caminho))throw new IllegalArgumentException("Recurso inválido.");
        return enviar(HttpRequest.newBuilder(URI.create("https://api.mercadolibre.com"+caminho))
            .header("Authorization","Bearer "+token).header("Content-Type","application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(dados))));
    }
    private JsonNode enviar(HttpRequest.Builder builder) {
        try {
            var result=client.send(builder.timeout(Duration.ofSeconds(25)).header("Accept","application/json").build(),HttpResponse.BodyHandlers.ofString());
            if(result.statusCode()==400 && builder.build().uri().getPath().equals("/items/validate")) {
                var resposta=json.readTree(result.body());
                var codigos=new java.util.ArrayList<String>();
                for(var causa:resposta.path("cause")) {
                    var codigo=causa.path("code").asText();
                    if(codigo.matches("[A-Za-z0-9_.-]{1,120}"))codigos.add(codigo);
                }
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Revise os atributos exigidos pela categoria no Mercado Livre. "+String.join(", ",codigos.stream().limit(5).toList()));
            }
            if(result.statusCode()<200 || result.statusCode()>=300)
                throw new ResponseStatusException(result.statusCode()==429?HttpStatus.TOO_MANY_REQUESTS:HttpStatus.BAD_GATEWAY,
                    "Mercado Livre indisponível ou autorização recusada (HTTP "+result.statusCode()+"). Reconecte a conta se necessário.");
            return json.readTree(result.body().isBlank()?"{}":result.body());
        } catch(InterruptedException error) { Thread.currentThread().interrupt();throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Consulta interrompida."); }
        catch(java.io.IOException error) { throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Não foi possível consultar o Mercado Livre."); }
    }
    public static String encode(String texto) { return URLEncoder.encode(texto,StandardCharsets.UTF_8); }
}
