package dev.jotxee.secretsanta.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EmailCryptoService {
    private final SecretKeySpec secretKey;

    public EmailCryptoService(String key) {
        byte[] keyBytes = key.getBytes();
        byte[] keyPadded = new byte[16];
        System.arraycopy(keyBytes, 0, keyPadded, 0, Math.min(keyBytes.length, 16));
        this.secretKey = new SecretKeySpec(keyPadded, "AES");
    }

    public String encrypt(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting email", e);
        }
    }

    public String decrypt(String strToDecrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting email", e);
        }
    }
}
