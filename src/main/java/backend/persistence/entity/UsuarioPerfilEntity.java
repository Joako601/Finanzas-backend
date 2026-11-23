package backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "usuario_perfil")
public class UsuarioPerfilEntity {

    @Id
    @Column(name = "firebase_uid", nullable = false, length = 128)
    private String firebaseUid;

    @Column(nullable = false, length = 254)
    private String correo;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "ultimo_acceso_en", nullable = false)
    private Instant ultimoAccesoEn;

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Instant creadoEn) {
        this.creadoEn = creadoEn;
    }

    public Instant getUltimoAccesoEn() {
        return ultimoAccesoEn;
    }

    public void setUltimoAccesoEn(Instant ultimoAccesoEn) {
        this.ultimoAccesoEn = ultimoAccesoEn;
    }
}
