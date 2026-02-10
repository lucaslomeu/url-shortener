package org.lomeu.domain.validator;

import org.lomeu.domain.exception.InvalidUrlException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

public class UrlValidator {
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final int MAX_URL_LENGTH = 2048;

    public void validate(String url) {
        if (url == null || url.isBlank()) {
            throw new InvalidUrlException("URL must not be blank");
        }

        if (url.length() > MAX_URL_LENGTH) {
            throw new InvalidUrlException("URL too long (max 2048 chars)");
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Invalid URL format", e);
        }

        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
            throw new InvalidUrlException("Only http/https allowed");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new InvalidUrlException("URL must have valid host");
        }

        if (isPrivateHost(host)) {
            throw new InvalidUrlException("Private hosts not allowed");
        }
    }

    private boolean isPrivateHost(String host) {
        String normalized = host.toLowerCase();

        if ("localhost".equals(normalized)
                || isPrivateIpv4(normalized)
                || isPrivateIpv6(normalized)) {
            return true;
        }

        if (isIpv4MappedIpv6(normalized)) {
            String ipv4 = normalized.substring(normalized.lastIndexOf(':') + 1);
            return isPrivateIpv4(ipv4);
        }

        return false;
    }

    private boolean isPrivateIpv4(String host) {
        int[] octets = parseIpv4(host);
        if (octets == null) {
            return false;
        }

        int first = octets[0];
        int second = octets[1];

        return first == 10
                || first == 127
                || (first == 192 && second == 168)
                || (first == 172 && second >= 16 && second <= 31);
    }

    private int[] parseIpv4(String host) {
        if (!isIpv4Literal(host)) {
            return null;
        }

        String[] parts = host.split("\\.");
        int[] octets = new int[4];
        for (int i = 0; i < parts.length; i++) {
            int value;

            try {
                value = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                return null;
            }

            if (value < 0 || value > 255) {
                return null;
            }
            octets[i] = value;
        }
        return octets;
    }

    private boolean isIpv4Literal(String host) {
        return host.matches("\\d{1,3}(\\.\\d{1,3}){3}");
    }

    private boolean isPrivateIpv6(String host) {
        if (!host.contains(":")) {
            return false;
        }

        if ("::1".equals(host)
            || "0:0:0:0:0:0:0:1".equals(host)
            || host.startsWith("fc")
            || host.startsWith("fd")) {
            return true;
        }

        return host.startsWith("fe8")
            || host.startsWith("fe9")
            || host.startsWith("fea")
            || host.startsWith("feb");
    }

    private boolean isIpv4MappedIpv6(String host) {
        return host.contains(":") && host.contains(".");
    }
}
