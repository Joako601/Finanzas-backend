package backend.persistence.mapper;

import backend.domain.model.ProductoFinanciero;
import backend.persistence.entity.ProductoFinancieroEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface ProductoFinancieroMapper {

    ProductoFinanciero toDomain(ProductoFinancieroEntity entity);

    List<ProductoFinanciero> toDomainList(List<ProductoFinancieroEntity> entities);

    @Mapping(target = "activoUnico", expression = "java(domain.activo() ? Boolean.TRUE : null)")
    ProductoFinancieroEntity toEntity(ProductoFinanciero domain);
}
