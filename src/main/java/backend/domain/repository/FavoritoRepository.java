package backend.domain.repository;

import backend.domain.model.ProductoFinanciero;

import java.util.List;

/** Puerto de persistencia atómico e idempotente para productos favoritos de un usuario. */
public interface FavoritoRepository {

    List<ProductoFinanciero> listarActivosDeUsuario(String firebaseUid);

    void agregarSiNoExiste(String firebaseUid, Long productoId);

    void eliminar(String firebaseUid, Long productoId);
}
