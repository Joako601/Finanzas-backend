package backend.persistence;

import backend.domain.model.TipoCambio;
import backend.domain.repository.TipoCambioRepository;
import backend.persistence.crud.TipoCambioJpaRepository;
import backend.persistence.entity.TipoCambioEntity;
import backend.persistence.mapper.TipoCambioMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TipoCambioRepositoryAdapter implements TipoCambioRepository {

    private final TipoCambioJpaRepository jpaRepository;
    private final TipoCambioMapper mapper;

    public TipoCambioRepositoryAdapter(TipoCambioJpaRepository jpaRepository, TipoCambioMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TipoCambio> buscarMasReciente(String base, String destino) {
        return jpaRepository.findFirstByMonedaBaseAndMonedaDestinoOrderByFechaDescActualizadoEnDesc(base, destino)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoCambio> buscarHistorico(String base, String destino, LocalDate desde, LocalDate hasta) {
        // Deduplica por fecha conservando el registro más reciente si coexisten dos fuentes el mismo día.
        List<TipoCambioEntity> filas = jpaRepository
                .findByMonedaBaseAndMonedaDestinoAndFechaBetweenOrderByFechaAscActualizadoEnDesc(
                        base, destino, desde, hasta);
        Map<LocalDate, TipoCambio> porFecha = new LinkedHashMap<>();
        for (TipoCambioEntity fila : filas) {
            porFecha.putIfAbsent(fila.getFecha(), mapper.toDomain(fila));
        }
        return List.copyOf(porFecha.values());
    }

    @Override
    @Transactional
    public void guardarOActualizar(TipoCambio tc) {
        persistirUno(tc);
    }

    @Override
    @Transactional
    public void guardarOActualizarTodos(List<TipoCambio> tiposCambio) {
        if (tiposCambio == null || tiposCambio.isEmpty()) {
            return;
        }
        for (TipoCambio tc : tiposCambio) {
            persistirUno(tc);
        }
    }

    private void persistirUno(TipoCambio tc) {
        int insertados = jpaRepository.insertarSiNoExiste(
                tc.monedaBase(),
                tc.monedaDestino(),
                tc.fecha(),
                tc.tasa(),
                tc.fuente(),
                tc.actualizadoEn());
        if (insertados == 0) {
            jpaRepository.actualizarTasa(
                    tc.monedaBase(),
                    tc.monedaDestino(),
                    tc.fecha(),
                    tc.fuente(),
                    tc.tasa(),
                    tc.actualizadoEn());
        }
    }
}
