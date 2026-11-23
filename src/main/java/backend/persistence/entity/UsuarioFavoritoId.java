package backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UsuarioFavoritoId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "firebase_uid", nullable = false, length = 128)
    private String firebaseUid;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    public UsuarioFavoritoId() {
    }

    public UsuarioFavoritoId(String firebaseUid, Long productoId) {
        this.firebaseUid = firebaseUid;
        this.productoId = productoId;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioFavoritoId that)) return false;
        return Objects.equals(firebaseUid, that.firebaseUid)
                && Objects.equals(productoId, that.productoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firebaseUid, productoId);
    }
}
