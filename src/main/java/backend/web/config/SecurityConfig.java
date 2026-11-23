package backend.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Seguridad sin estado (Stateless) respaldada por Firebase Authentication ID Tokens (JWT).
 *
 * Verifica firma contra las llaves públicas JWKS de Google/Firebase, además de `iss`, `aud`
 * (ID de proyecto Firebase), `exp`, `iat` en el pasado y `sub` válido (`1..128`), sin almacenar
 * contraseñas en PostgreSQL.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String FIREBASE_JWKS_URI =
            "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";
    private static final Duration MAX_CLOCK_SKEW = Duration.ofSeconds(60);
    private static final int MAX_SUBJECT_LENGTH = 128;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            @Value("${finanzas.openapi.enabled:false}") boolean openApiEnabled) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .headers(headers -> headers
                        .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/health").permitAll();
                    if (openApiEnabled) {
                        auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll();
                    }
                    auth.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/**").authenticated()
                            .anyRequest().denyAll();
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
                            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.getWriter().write("""
                                    {"title":"Unauthorized","status":401,"detail":"Se requiere una sesión válida para acceder a este recurso."}""");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.getWriter().write("""
                                    {"title":"Forbidden","status":403,"detail":"No tienes permisos para realizar esta acción."}""");
                        })
                );

        return http.build();
    }

    @Bean
    public JwtDecoder firebaseJwtDecoder(
            @Value("${finanzas.firebase.project-id:}") String projectId) {
        String limpio = validarProjectId(projectId);
        HttpClient jwksHttp = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        JdkClientHttpRequestFactory jwksFactory = new JdkClientHttpRequestFactory(jwksHttp);
        jwksFactory.setReadTimeout(Duration.ofSeconds(5));
        RestTemplate jwksClient = new RestTemplate(jwksFactory);

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(FIREBASE_JWKS_URI)
                .restOperations(jwksClient)
                .build();
        decoder.setJwtValidator(construirValidadorFirebase(limpio));
        return decoder;
    }

    static String validarProjectId(String projectId) {
        if (projectId == null || projectId.isBlank() || projectId.trim().startsWith("tu-proyecto")) {
            throw new IllegalStateException(
                    "finanzas.firebase.project-id (FIREBASE_PROJECT_ID) es obligatorio y no puede quedar vacío.");
        }
        return projectId.trim();
    }

    static OAuth2TokenValidator<Jwt> construirValidadorFirebase(String projectId) {
        String expectedIssuer = "https://securetoken.google.com/" + projectId;
        OAuth2TokenValidator<Jwt> issuerAndTimestamp = JwtValidators.createDefaultWithIssuer(expectedIssuer);

        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            List<String> aud = jwt.getAudience();
            if (aud != null && aud.contains(projectId)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "La audiencia del token no corresponde al proyecto.", null));
        };

        OAuth2TokenValidator<Jwt> subjectValidator = jwt -> {
            String sub = jwt.getSubject();
            if (sub != null && !sub.isBlank() && sub.length() <= MAX_SUBJECT_LENGTH) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "El identificador de usuario del token es inválido.", null));
        };

        OAuth2TokenValidator<Jwt> issuedAtValidator = jwt -> {
            Instant iat = jwt.getIssuedAt();
            if (iat != null && !iat.isAfter(Instant.now().plus(MAX_CLOCK_SKEW))) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "La marca de emisión del token es inválida.", null));
        };

        return new DelegatingOAuth2TokenValidator<>(
                issuerAndTimestamp, audienceValidator, subjectValidator, issuedAtValidator);
    }

    @Bean
    public JwtAuthenticationConverter firebaseJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(SecurityConfig::extraerAutoridades);
        return converter;
    }

    /**
     * Asigna `ROLE_USER` a todo token válido y `ROLE_ADMIN` únicamente cuando el custom claim
     * estándar de Firebase `role` equivale a `"ADMIN"`.
     */
    static Collection<GrantedAuthority> extraerAutoridades(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        Object roleClaim = jwt.getClaims().get("role");
        if (roleClaim instanceof String role && "ADMIN".equalsIgnoreCase(role.trim())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return authorities;
    }
}
