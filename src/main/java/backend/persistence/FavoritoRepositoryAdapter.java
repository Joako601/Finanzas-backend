package backend.persistence;

import backend.domain.model.ProductoFinanciero;
import backend.domain.repository.FavoritoRepository;
import backend.persistence.crud.UsuarioFavoritoJpaRepository;
import backend.persistence.mapper.ProductoFinancieroMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class FavoritoRepositoryAdapter implements FavoritoRepository {

    private final UsuarioFavoritoJpaRepository favoritoJpa;
    private final ProductoFinancieroMapper productoMapper;

    public FavoritoRepositoryAdapter(
            UsuarioFavoritoJpaRepository favoritoJpa,
            ProductoFinancieroMapper productoMapper) {
        this.favoritoJpa = favoritoJpa;
        this.productoMapper = productoMapper;
    }

    @Override
    public List<ProductoFinanciero> listarActivosDeUsuario(String firebaseUid) {
        return productoMapper.toDomainList(favoritoJpa.listarProductosActivosPorUid(firebaseUid));
    }

    @Override
    public void agregarSiNoExiste(String firebaseUid, Long productoId) {
        favoritoJpa.agregarSiNoExiste(firebaseUid, productoId, Instant.now());
    }

    @Override
    public void eliminar(String firebaseUid, Long productoId) {
        favoritoJpa.eliminarPorUidYProductoId(firebaseUid, productoId);
    }
}
