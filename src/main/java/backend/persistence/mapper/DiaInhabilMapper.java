package backend.persistence.mapper;

import backend.domain.model.DiaInhabil;
import backend.persistence.entity.DiaInhabilEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface DiaInhabilMapper {

    DiaInhabil toDomain(DiaInhabilEntity entity);

    List<DiaInhabil> toDomainList(List<DiaInhabilEntity> entities);
}
