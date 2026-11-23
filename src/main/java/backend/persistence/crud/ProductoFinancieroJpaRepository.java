package backend.persistence.crud;

import backend.persistence.entity.ProductoFinancieroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoFinancieroJpaRepository extends JpaRepository<ProductoFinancieroEntity, Long> {

    @Query("""
            SELECT p FROM ProductoFinancieroEntity p
            WHERE p.activo = TRUE
              AND (:categoria = '' OR LOWER(p.categoria) = :categoria)
              AND (:patron = '' OR
                   LOWER(p.nombre) LIKE :patron ESCAPE '!' OR
                   LOWER(p.entidad) LIKE :patron ESCAPE '!' OR
                   LOWER(p.detalleReferencia) LIKE :patron ESCAPE '!')
            ORDER BY p.id ASC
            """)
    List<ProductoFinancieroEntity> buscarActivosFiltrados(
            @Param("categoria") String categoriaEnMinusculasOVacio,
            @Param("patron") String patronLikeOVacio);

    Optional<ProductoFinancieroEntity> findByIdAndActivoTrue(Long id);

    List<ProductoFinancieroEntity> findByIdInAndActivoTrue(List<Long> ids);
}
