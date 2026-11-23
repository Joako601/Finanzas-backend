package backend.domain.repository;

import backend.domain.model.PerfilUsuario;

import java.time.Instant;

/** Puerto de persistencia para perfiles mínimos sincronizados atómicamente desde Firebase JWT. */
public interface UsuarioPerfilRepository {

    PerfilUsuario sincronizar(String firebaseUid, String correo, String rol, Instant ahora);
}
