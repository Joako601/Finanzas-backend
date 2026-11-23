package backend.persistence.mapper;

import backend.domain.model.TipoCambio;
import backend.persistence.entity.TipoCambioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TipoCambioMapper {

    @Mapping(target = "desdeCache", constant = "true")
    TipoCambio toDomain(TipoCambioEntity entity);
}
