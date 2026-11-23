package backend.persistence.crud;

import backend.persistence.entity.UsuarioPerfilEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface UsuarioPerfilJpaRepository extends JpaRepository<UsuarioPerfilEntity, String> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO usuario_perfil (firebase_uid, correo, rol, creado_en, ultimo_acceso_en)
            VALUES (:uid, :correo, :rol, :ahora, :ahora)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertarSiNoExiste(
            @Param("uid") String firebaseUid,
            @Param("correo") String correo,
            @Param("rol") String rol,
            @Param("ahora") Instant ahora);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE UsuarioPerfilEntity u
            SET u.correo = :correo, u.rol = :rol, u.ultimoAccesoEn = :ahora
            WHERE u.firebaseUid = :uid
            """)
    int actualizarAcceso(
            @Param("uid") String firebaseUid,
            @Param("correo") String correo,
            @Param("rol") String rol,
            @Param("ahora") Instant ahora);
}
