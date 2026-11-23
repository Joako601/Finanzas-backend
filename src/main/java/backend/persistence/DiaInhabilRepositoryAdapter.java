package backend.persistence;

import backend.domain.model.DiaInhabil;
import backend.domain.repository.DiaInhabilRepository;
import backend.persistence.crud.DiaInhabilJpaRepository;
import backend.persistence.mapper.DiaInhabilMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class DiaInhabilRepositoryAdapter implements DiaInhabilRepository {

    private final DiaInhabilJpaRepository jpaRepository;
    private final DiaInhabilMapper mapper;

    public DiaInhabilRepositoryAdapter(DiaInhabilJpaRepository jpaRepository, DiaInhabilMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiaInhabil> listarPorAnio(int anio) {
        return mapper.toDomainList(jpaRepository.findByAnioOrderByFechaAscTipoAsc(anio));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Instant> ultimaActualizacion(int anio) {
        return jpaRepository.ultimaActualizacionPorAnio(anio);
    }

    @Override
    @Transactional
    public void guardarSiNoExiste(List<DiaInhabil> dias) {
        for (DiaInhabil dia : dias) {
            jpaRepository.insertarSiNoExiste(
                    dia.fecha(),
                    dia.nombre(),
                    dia.tipo(),
                    dia.anio(),
                    dia.fuente(),
                    dia.actualizadoEn());
        }
    }

    @Override
    @Transactional
    public void guardarOActualizar(List<DiaInhabil> dias) {
        for (DiaInhabil dia : dias) {
            int insertados = jpaRepository.insertarSiNoExiste(
                    dia.fecha(),
                    dia.nombre(),
                    dia.tipo(),
                    dia.anio(),
                    dia.fuente(),
                    dia.actualizadoEn());
            if (insertados == 0) {
                jpaRepository.actualizarPorFechaYTipo(
                        dia.fecha(),
                        dia.tipo(),
                        dia.nombre(),
                        dia.anio(),
                        dia.fuente(),
                        dia.actualizadoEn());
            }
        }
    }
}
