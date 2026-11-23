package backend.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    private static final String PROJECT_ID = "finanzas-test";

    @Test
    void validarProjectIdRechazaVacioYPlaceholder() {
        assertThrows(IllegalStateException.class, () -> SecurityConfig.validarProjectId(""));
        assertThrows(IllegalStateException.class, () -> SecurityConfig.validarProjectId("   "));
        assertThrows(IllegalStateException.class, () -> SecurityConfig.validarProjectId("tu-proyecto-firebase"));
        assertEquals(PROJECT_ID, SecurityConfig.validarProjectId("  " + PROJECT_ID + "  "));
    }

    @Test
    void validadorFirebaseAceptaTokenValidoYRechazaClaimsInvalidos() {
        OAuth2TokenValidator<Jwt> validator = SecurityConfig.construirValidadorFirebase(PROJECT_ID);
        Instant now = Instant.now();

        Jwt valido = jwtBase(
                "https://securetoken.google.com/" + PROJECT_ID,
                List.of(PROJECT_ID),
                "uid-123",
                now.minusSeconds(10),
                now.plusSeconds(3600));
        assertFalse(validator.validate(valido).hasErrors());

        Jwt audIncorrecta = jwtBase(
                "https://securetoken.google.com/" + PROJECT_ID,
                List.of("otro-proyecto"),
                "uid-123",
                now.minusSeconds(10),
                now.plusSeconds(3600));
        assertTrue(validator.validate(audIncorrecta).hasErrors());

        Jwt issIncorrecto = jwtBase(
                "https://securetoken.google.com/otro-proyecto",
                List.of(PROJECT_ID),
                "uid-123",
                now.minusSeconds(10),
                now.plusSeconds(3600));
        assertTrue(validator.validate(issIncorrecto).hasErrors());

        Jwt iatFuturo = jwtBase(
                "https://securetoken.google.com/" + PROJECT_ID,
                List.of(PROJECT_ID),
                "uid-123",
                now.plusSeconds(300),
                now.plusSeconds(3600));
        assertTrue(validator.validate(iatFuturo).hasErrors());

        Jwt subMuyLargo = jwtBase(
                "https://securetoken.google.com/" + PROJECT_ID,
                List.of(PROJECT_ID),
                "a".repeat(129),
                now.minusSeconds(10),
                now.plusSeconds(3600));
        assertTrue(validator.validate(subMuyLargo).hasErrors());
    }

    @Test
    void extraerAutoridadesAsignaUserPorDefectoYAdminSoloConClaimRoleAdmin() {
        Instant now = Instant.now();
        Jwt usuarioNormal = new Jwt(
                "token", now.minusSeconds(5), now.plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of("sub", "uid-1", "role", "USER"));
        Collection<GrantedAuthority> authUser = SecurityConfig.extraerAutoridades(usuarioNormal);
        assertEquals(List.of("ROLE_USER"), authUser.stream().map(GrantedAuthority::getAuthority).toList());

        Jwt admin = new Jwt(
                "token", now.minusSeconds(5), now.plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of("sub", "uid-2", "role", "ADMIN"));
        Collection<GrantedAuthority> authAdmin = SecurityConfig.extraerAutoridades(admin);
        assertEquals(List.of("ROLE_USER", "ROLE_ADMIN"), authAdmin.stream().map(GrantedAuthority::getAuthority).toList());

        Jwt tipoInesperado = new Jwt(
                "token", now.minusSeconds(5), now.plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of("sub", "uid-3", "role", 12345));
        Collection<GrantedAuthority> authSeguro = SecurityConfig.extraerAutoridades(tipoInesperado);
        assertEquals(List.of("ROLE_USER"), authSeguro.stream().map(GrantedAuthority::getAuthority).toList());
    }

    private static Jwt jwtBase(String iss, List<String> aud, String sub, Instant iat, Instant exp) {
        return new Jwt(
                "token",
                iat,
                exp,
                Map.of("alg", "RS256"),
                Map.of("iss", iss, "aud", aud, "sub", sub));
    }
}
