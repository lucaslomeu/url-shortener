package org.lomeu.infrastructure.generator;

import org.lomeu.application.port.out.ShortCodeGeneratorPort;

import java.security.SecureRandom;

public class ShortCodeGenerator implements ShortCodeGeneratorPort {

    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(BASE62.charAt(RANDOM.nextInt(BASE62.length())));
        }
        return sb.toString();
    }
}
