package backend.persistence.mapper;

import backend.domain.model.PerfilUsuario;
import backend.persistence.entity.UsuarioPerfilEntity;
import org.mapstruct.Mapper;

@Mapper
public interface UsuarioPerfilMapper {

    PerfilUsuario toDomain(UsuarioPerfilEntity entity);

    UsuarioPerfilEntity toEntity(PerfilUsuario domain);
}
