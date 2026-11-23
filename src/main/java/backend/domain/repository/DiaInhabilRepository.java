package backend.domain.repository;

import backend.domain.model.DiaInhabil;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DiaInhabilRepository {

    List<DiaInhabil> listarPorAnio(int anio);

    Optional<Instant> ultimaActualizacion(int anio);

    void guardarSiNoExiste(List<DiaInhabil> dias);

    void guardarOActualizar(List<DiaInhabil> dias);
}
