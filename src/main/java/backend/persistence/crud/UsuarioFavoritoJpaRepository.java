package backend.persistence.crud;

import backend.persistence.entity.ProductoFinancieroEntity;
import backend.persistence.entity.UsuarioFavoritoEntity;
import backend.persistence.entity.UsuarioFavoritoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface UsuarioFavoritoJpaRepository extends JpaRepository<UsuarioFavoritoEntity, UsuarioFavoritoId> {

    @Query("""
            SELECT f.producto FROM UsuarioFavoritoEntity f
            WHERE f.id.firebaseUid = :uid AND f.producto.activo = TRUE
            ORDER BY f.creadoEn DESC, f.id.productoId ASC
            """)
    List<ProductoFinancieroEntity> listarProductosActivosPorUid(@Param("uid") String firebaseUid);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO usuario_favorito (firebase_uid, producto_id, creado_en)
            VALUES (:uid, :productoId, :creadoEn)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int agregarSiNoExiste(
            @Param("uid") String firebaseUid,
            @Param("productoId") Long productoId,
            @Param("creadoEn") Instant creadoEn);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UsuarioFavoritoEntity f WHERE f.id.firebaseUid = :uid AND f.id.productoId = :productoId")
    int eliminarPorUidYProductoId(
            @Param("uid") String firebaseUid,
            @Param("productoId") Long productoId);
}
