package com.isac.bazar_solidario.service;

import tools.jackson.databind.*;
import com.isac.bazar_solidario.config.AsaasConfig;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.math.BigDecimal;

/** Não segue redirects, não registra payload/credenciais e não aceita URL fornecida pelo cliente. */
@Component
public class AsaasGateway {
    private final AsaasConfig config;
    private final ObjectMapper json=new ObjectMapper();
    private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    public AsaasGateway(AsaasConfig config){this.config=config;}
    public JsonNode consultar(String caminho){return chamar("GET",caminho,null);}
    public JsonNode cobrar(String referencia,String nome,String email,String cpf,BigDecimal total,BigDecimal comissao) {
        var wallets=consultar("/wallets/");
        if(wallets.toString().contains(config.getIsacWalletId()))
            throw new IllegalStateException("A carteira do ISAC precisa ser diferente da conta emissora.");
        var clientes=consultar("/customers?externalReference="+referencia);
        String cliente;
        if(clientes.path("data").size()>0) cliente=clientes.path("data").get(0).path("id").asText();
        else cliente=chamar("POST","/customers",Map.of("name",nome,"email",email,"cpfCnpj",cpf,
            "externalReference",referencia,"notificationDisabled",true)).path("id").asText();
        if(cliente.isBlank()) throw new IllegalStateException("O provedor não retornou o pagador.");
        return chamar("POST","/payments",Map.of("customer",cliente,"billingType","PIX","value",total,
            "dueDate",LocalDate.now(ZoneId.of("America/Recife")).plusDays(1).toString(),
            "externalReference",referencia,"description","ISAC Brechó — compra de teste",
            "split",List.of(Map.of("walletId",config.getIsacWalletId(),"fixedValue",comissao))));
    }
    private JsonNode chamar(String metodo,String caminho,Object corpo) {
        if(!config.getBaseUrl().equals("https://api-sandbox.asaas.com/v3"))
            throw new IllegalStateException("Este piloto aceita somente o Sandbox.");
        try {
            var builder=HttpRequest.newBuilder(URI.create(config.getBaseUrl()+caminho))
                .timeout(Duration.ofSeconds(25)).header("access_token",config.getApiKey())
                .header("User-Agent","ISAC-Brecho-Sandbox/1.0").header("accept","application/json");
            if(corpo==null) builder.GET();
            else builder.header("Content-Type","application/json").method(metodo,HttpRequest.BodyPublishers.ofString(json.writeValueAsString(corpo)));
            var response=http.send(builder.build(),HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()<200 || response.statusCode()>=300)
                throw new IllegalStateException("O Asaas recusou a operação (HTTP "+response.statusCode()+"). Confira o cadastro e a configuração.");
            return json.readTree(response.body());
        } catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("Consulta interrompida; reconcilie antes de repetir.");}
        catch(java.io.IOException e){throw new IllegalStateException("Resposta inconclusiva do Asaas; consulte o pedido antes de repetir.");}
    }
}
