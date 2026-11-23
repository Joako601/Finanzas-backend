package backend.domain.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validación estricta de URLs HTTPS públicas para imágenes del catálogo (defensa OWASP contra
 * CRLF, credenciales embebidas, hosts locales/loopback e IPs privadas).
 */
public final class ValidadorUrlHttps {

    private static final Pattern IPV4_LITERAL = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");

    private ValidadorUrlHttps() {
    }

    public static boolean esValidaOOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return true;
        }
        String limpia = valor.trim();
        if (limpia.length() > 500) {
            return false;
        }
        if (limpia.chars().anyMatch(c -> Character.isWhitespace(c) || Character.isISOControl(c))) {
            return false;
        }
        final URI uri;
        try {
            uri = new URI(limpia);
        } catch (URISyntaxException e) {
            return false;
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            return false;
        }
        if (uri.getRawUserInfo() != null) {
            return false;
        }
        int puerto = uri.getPort();
        if (puerto != -1 && puerto != 443) {
            return false;
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return false;
        }
        String hostLower = host.toLowerCase(Locale.ROOT);
        if (hostLower.equals("localhost")
                || hostLower.endsWith(".localhost")
                || hostLower.endsWith(".local")
                || hostLower.endsWith(".internal")
                || !hostLower.contains(".")
                || hostLower.contains(":")
                || hostLower.startsWith("[")
                || IPV4_LITERAL.matcher(hostLower).matches()) {
            return false;
        }
        return true;
    }
}
