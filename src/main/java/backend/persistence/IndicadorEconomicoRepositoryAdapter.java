package backend.persistence;

import backend.domain.model.IndicadorEconomico;
import backend.domain.repository.IndicadorEconomicoRepository;
import backend.persistence.crud.IndicadorEconomicoJpaRepository;
import backend.persistence.mapper.IndicadorEconomicoMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class IndicadorEconomicoRepositoryAdapter implements IndicadorEconomicoRepository {

    private final IndicadorEconomicoJpaRepository jpaRepository;
    private final IndicadorEconomicoMapper mapper;

    public IndicadorEconomicoRepositoryAdapter(
            IndicadorEconomicoJpaRepository jpaRepository,
            IndicadorEconomicoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IndicadorEconomico> buscarMasReciente(String clave) {
        return jpaRepository.findFirstByClaveOrderByFechaDesc(clave).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IndicadorEconomico> listarUltimos() {
        return jpaRepository.listarUltimosPorClave().stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void guardarOActualizar(IndicadorEconomico ind) {
        int insertados = jpaRepository.insertarSiNoExiste(
                ind.clave(),
                ind.nombre(),
                ind.valor(),
                ind.unidad(),
                ind.fecha(),
                ind.fuente(),
                ind.serieExterna(),
                ind.actualizadoEn());
        if (insertados == 0) {
            jpaRepository.actualizarValor(
                    ind.clave(),
                    ind.fecha(),
                    ind.nombre(),
                    ind.valor(),
                    ind.unidad(),
                    ind.fuente(),
                    ind.serieExterna(),
                    ind.actualizadoEn());
        }
    }
}
