package backend.domain.repository;

import backend.domain.model.ProductoFinanciero;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia para el catálogo público de productos financieros. */
public interface ProductoFinancieroRepository {

    List<ProductoFinanciero> listarActivos(String categoria, String busqueda);

    Optional<ProductoFinanciero> buscarActivoPorId(Long id);

    List<ProductoFinanciero> buscarActivosPorIds(List<Long> ids);

    ProductoFinanciero guardar(ProductoFinanciero producto);
}
