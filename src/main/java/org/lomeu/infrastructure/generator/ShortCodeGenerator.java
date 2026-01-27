package org.lomeu.infrastructure.generator;

import org.lomeu.application.port.out.ShortCodeGeneratorPort;

public class ShortCodeGenerator implements ShortCodeGeneratorPort {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    @Override
    public String generate(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }

        StringBuilder sb = new StringBuilder();
        long n = id;

        while (n > 0) {
            int remainder = (int) (n % 62);
            sb.append(BASE62.charAt(remainder));
            n /= 62;
        }

        return sb.reverse().toString();
    }
}
