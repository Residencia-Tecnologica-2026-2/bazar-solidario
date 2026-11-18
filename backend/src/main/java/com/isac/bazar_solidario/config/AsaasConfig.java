package com.isac.bazar_solidario.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

/** Piloto exclusivamente Sandbox: uma conta emissora vinculada a uma loja local. */
@Component @Getter
public class AsaasConfig {
    @Value("${app.asaas.api-key:}") private String apiKey;
    @Value("${app.asaas.base-url:https://api-sandbox.asaas.com/v3}") private String baseUrl;
    @Value("${app.asaas.seller-id:0}") private long sellerId;
    @Value("${app.asaas.isac-wallet-id:}") private String isacWalletId;
    @Value("${app.asaas.webhook-token:}") private String webhookToken;
    public boolean configurado() {
        return !apiKey.isBlank() && sellerId > 0
            && isacWalletId.matches("[a-fA-F0-9-]{36}")
            && baseUrl.equals("https://api-sandbox.asaas.com/v3");
    }
}
