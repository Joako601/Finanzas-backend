package backend.persistence.mapper;

import backend.domain.model.IndicadorEconomico;
import backend.persistence.entity.IndicadorEconomicoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface IndicadorEconomicoMapper {

    @Mapping(target = "desdeCache", constant = "true")
    IndicadorEconomico toDomain(IndicadorEconomicoEntity entity);
}
