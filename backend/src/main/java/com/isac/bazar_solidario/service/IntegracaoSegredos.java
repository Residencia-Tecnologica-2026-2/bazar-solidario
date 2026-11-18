package com.isac.bazar_solidario.service;

import com.isac.bazar_solidario.config.MercadoLivreConfig;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.nio.ByteBuffer;

@Component
public class IntegracaoSegredos {
    private final MercadoLivreConfig config;
    private final SecureRandom random = new SecureRandom();
    public IntegracaoSegredos(MercadoLivreConfig config) { this.config=config; }
    public String cifrar(String texto) {
        try {
            byte[] iv=new byte[12]; random.nextBytes(iv);
            var cipher=cipher(Cipher.ENCRYPT_MODE,iv);
            var bytes=cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length+bytes.length).put(iv).put(bytes).array());
        } catch(Exception error) { throw new IllegalStateException("Não foi possível proteger as credenciais da integração."); }
    }
    public String abrir(String texto) {
        try {
            var bytes=ByteBuffer.wrap(Base64.getDecoder().decode(texto));
            byte[] iv=new byte[12]; bytes.get(iv);
            byte[] dados=new byte[bytes.remaining()]; bytes.get(dados);
            return new String(cipher(Cipher.DECRYPT_MODE,iv).doFinal(dados),StandardCharsets.UTF_8);
        } catch(Exception error) { throw new IllegalStateException("Credencial indisponível. Confira a chave de criptografia da integração."); }
    }
    private Cipher cipher(int modo,byte[] iv) throws Exception {
        var cipher=Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(modo,new SecretKeySpec(Base64.getDecoder().decode(config.getEncryptionKey()),"AES"),new GCMParameterSpec(128,iv));
        return cipher;
    }
}
