package backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "usuario_favorito")
public class UsuarioFavoritoEntity {

    @EmbeddedId
    private UsuarioFavoritoId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("firebaseUid")
    @JoinColumn(name = "firebase_uid", nullable = false)
    private UsuarioPerfilEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("productoId")
    @JoinColumn(name = "producto_id", nullable = false)
    private ProductoFinancieroEntity producto;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    public UsuarioFavoritoId getId() {
        return id;
    }

    public void setId(UsuarioFavoritoId id) {
        this.id = id;
    }

    public UsuarioPerfilEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioPerfilEntity usuario) {
        this.usuario = usuario;
    }

    public ProductoFinancieroEntity getProducto() {
        return producto;
    }

    public void setProducto(ProductoFinancieroEntity producto) {
        this.producto = producto;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Instant creadoEn) {
        this.creadoEn = creadoEn;
    }
}
