package backend.domain.model;

import java.time.Instant;

/** Perfil mínimo del usuario autenticado vía Firebase Auth (sin contraseñas ni datos bancarios). */
public record PerfilUsuario(
        String firebaseUid,
        String correo,
        String rol,
        Instant creadoEn,
        Instant ultimoAccesoEn
) {
}
