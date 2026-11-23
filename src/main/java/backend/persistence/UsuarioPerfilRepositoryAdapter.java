package backend.persistence;

import backend.domain.model.PerfilUsuario;
import backend.domain.repository.UsuarioPerfilRepository;
import backend.persistence.crud.UsuarioPerfilJpaRepository;
import backend.persistence.mapper.UsuarioPerfilMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class UsuarioPerfilRepositoryAdapter implements UsuarioPerfilRepository {

    private final UsuarioPerfilJpaRepository jpaRepository;
    private final UsuarioPerfilMapper mapper;

    public UsuarioPerfilRepositoryAdapter(
            UsuarioPerfilJpaRepository jpaRepository,
            UsuarioPerfilMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public PerfilUsuario sincronizar(String firebaseUid, String correo, String rol, Instant ahora) {
        int insertados = jpaRepository.insertarSiNoExiste(firebaseUid, correo, rol, ahora);
        if (insertados == 0) {
            jpaRepository.actualizarAcceso(firebaseUid, correo, rol, ahora);
        }
        return jpaRepository.findById(firebaseUid)
                .map(mapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar el perfil recién sincronizado."));
    }
}
