package backend.persistence;

import backend.domain.model.ProductoFinanciero;
import backend.domain.repository.ProductoFinancieroRepository;
import backend.persistence.crud.ProductoFinancieroJpaRepository;
import backend.persistence.entity.ProductoFinancieroEntity;
import backend.persistence.mapper.ProductoFinancieroMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public class ProductoFinancieroRepositoryAdapter implements ProductoFinancieroRepository {

    private final ProductoFinancieroJpaRepository jpaRepository;
    private final ProductoFinancieroMapper mapper;

    public ProductoFinancieroRepositoryAdapter(
            ProductoFinancieroJpaRepository jpaRepository,
            ProductoFinancieroMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<ProductoFinanciero> listarActivos(String categoria, String busqueda) {
        String categoriaFiltro = (categoria == null || categoria.isBlank())
                ? ""
                : categoria.trim().toLowerCase(Locale.ROOT);
        String patron = (busqueda == null || busqueda.isBlank())
                ? ""
                : "%" + escaparLike(busqueda.trim().toLowerCase(Locale.ROOT)) + "%";
        return mapper.toDomainList(jpaRepository.buscarActivosFiltrados(categoriaFiltro, patron));
    }

    @Override
    public Optional<ProductoFinanciero> buscarActivoPorId(Long id) {
        return jpaRepository.findByIdAndActivoTrue(id).map(mapper::toDomain);
    }

    @Override
    public List<ProductoFinanciero> buscarActivosPorIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mapper.toDomainList(jpaRepository.findByIdInAndActivoTrue(ids));
    }

    @Override
    public ProductoFinanciero guardar(ProductoFinanciero producto) {
        ProductoFinancieroEntity entity = mapper.toEntity(producto);
        return mapper.toDomain(jpaRepository.saveAndFlush(entity));
    }

    private static String escaparLike(String texto) {
        return texto
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }
}
