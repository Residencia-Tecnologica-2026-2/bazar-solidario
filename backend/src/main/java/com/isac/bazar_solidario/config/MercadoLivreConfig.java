package com.isac.bazar_solidario.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;
import java.net.URI;
import java.util.Base64;

@Component @Getter
public class MercadoLivreConfig {
    @Value("${ML_CLIENT_ID:}") private String clientId;
    @Value("${ML_CLIENT_SECRET:}") private String clientSecret;
    @Value("${ML_REDIRECT_URI:}") private String redirectUri;
    @Value("${ML_ENCRYPTION_KEY:}") private String encryptionKey;
    @Value("${PUBLIC_APP_URL:}") private String publicAppUrl;

    public boolean configurado() {
        try {
            var uri = URI.create(redirectUri);
            return clientId.matches("[0-9]+") && !clientSecret.isBlank()
                && "https".equals(uri.getScheme()) && uri.getHost() != null
                && uri.getUserInfo() == null && uri.getQuery() == null && uri.getFragment() == null
                && Base64.getDecoder().decode(encryptionKey).length == 32;
        } catch (RuntimeException error) { return false; }
    }
}
