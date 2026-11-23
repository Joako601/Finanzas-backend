package backend.web.controller;

import backend.domain.model.PerfilUsuario;
import backend.domain.service.UsuarioService;
import backend.web.config.OpenApiConfig;
import backend.web.dto.PerfilUsuarioResponse;
import backend.web.dto.ProductoFinancieroResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints del usuario autenticado. El identificador siempre se extrae del Firebase JWT
 * verificado (`jwt.getSubject()`) y jamás de parámetros de la solicitud (protección anti-IDOR).
 */
@Validated
@RestController
@RequestMapping("/api/v1/usuarios/me")
@Tag(name = "Usuario y favoritos", description = "Perfil mínimo y productos favoritos del usuario autenticado")
@SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Obtener y sincronizar el perfil mínimo del usuario autenticado")
    public ResponseEntity<PerfilUsuarioResponse> miPerfil(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication) {
        PerfilUsuario perfil = usuarioService.sincronizarPerfil(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                tieneRolAdmin(authentication));
        return ResponseEntity.ok(PerfilUsuarioResponse.desde(perfil));
    }

    @GetMapping("/favoritos")
    @Operation(summary = "Listar los productos financieros favoritos del usuario")
    public ResponseEntity<List<ProductoFinancieroResponse>> listarFavoritos(
            @AuthenticationPrincipal Jwt jwt) {
        List<ProductoFinancieroResponse> favoritos = usuarioService.listarFavoritos(jwt.getSubject())
                .stream()
                .map(ProductoFinancieroResponse::desde)
                .toList();
        return ResponseEntity.ok(favoritos);
    }

    @PostMapping("/favoritos/{productoId}")
    @Operation(summary = "Marcar un producto del catálogo como favorito (idempotente)")
    public ResponseEntity<Void> agregarFavorito(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication,
            @PathVariable @Positive Long productoId) {
        usuarioService.agregarFavorito(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                tieneRolAdmin(authentication),
                productoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/favoritos/{productoId}")
    @Operation(summary = "Quitar un producto de los favoritos del usuario")
    public ResponseEntity<Void> eliminarFavorito(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable @Positive Long productoId) {
        usuarioService.eliminarFavorito(jwt.getSubject(), productoId);
        return ResponseEntity.noContent().build();
    }

    private static boolean tieneRolAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
