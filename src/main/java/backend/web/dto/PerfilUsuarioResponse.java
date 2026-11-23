package backend.web.dto;

import backend.domain.model.PerfilUsuario;

import java.time.Instant;

/** Perfil mínimo del usuario autenticado vía Firebase Auth. */
public record PerfilUsuarioResponse(
        String uid,
        String correo,
        String rol,
        Instant creadoEn,
        Instant ultimoAccesoEn
) {
    public static PerfilUsuarioResponse desde(PerfilUsuario perfil) {
        return new PerfilUsuarioResponse(
                perfil.firebaseUid(),
                perfil.correo(),
                perfil.rol(),
                perfil.creadoEn(),
                perfil.ultimoAccesoEn()
        );
    }
}
