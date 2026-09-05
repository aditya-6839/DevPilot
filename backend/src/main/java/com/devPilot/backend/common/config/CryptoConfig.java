package com.devPilot.backend.common.config;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesGcmBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.nio.charset.StandardCharsets;

@Configuration
public class CryptoConfig {

    @Bean
    public TextEncryptor tokenEncryptor(
            @Value("${app.token-encryptor-password}") String password ,
            @Value("${app.token-encryptor-salt}") String salt
    ) {

        BytesEncryptor encryptor = AesGcmBytesEncryptor
                .withPassword(password , salt)
                .build();

        return new TextEncryptor() {

            @Override
            public @NonNull String encrypt(@NonNull String text) {
                byte[] encrypted = encryptor.encrypt(
                        text.getBytes(StandardCharsets.UTF_8)
                );

                return java.util.HexFormat.of().formatHex(encrypted);
            }

            @Override
            public @NonNull String decrypt(@NonNull String encryptedText) {
                byte[] encrypted =
                        java.util.HexFormat.of().parseHex(encryptedText);

                byte[] decrypted = encryptor.decrypt(encrypted);

                return new String(
                        decrypted ,
                        StandardCharsets.UTF_8
                );
            }
        };
    }
}
