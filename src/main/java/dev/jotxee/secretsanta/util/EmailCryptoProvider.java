package dev.jotxee.secretsanta.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailCryptoProvider {
    private final EmailCryptoService cryptoService;

    public EmailCryptoProvider(@Value("${app.email-crypto-key}") String key) {
        this.cryptoService = new EmailCryptoService(key);
    }

    public String encrypt(String email) {
        return cryptoService.encrypt(email);
    }

    public String decrypt(String encrypted) {
        return cryptoService.decrypt(encrypted);
    }
}
