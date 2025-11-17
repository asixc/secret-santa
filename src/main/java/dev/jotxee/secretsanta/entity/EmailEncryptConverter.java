package dev.jotxee.secretsanta.entity;

import dev.jotxee.secretsanta.util.EmailCryptoService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Converter
public class EmailEncryptConverter implements AttributeConverter<String, String> {
    public static String staticKey;

    public EmailEncryptConverter(@Value("${app.email-crypto-key}") String key) {
        staticKey = key;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return new EmailCryptoService(staticKey).encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return new EmailCryptoService(staticKey).decrypt(dbData);
    }
}
