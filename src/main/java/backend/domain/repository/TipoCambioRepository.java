package backend.domain.repository;

import backend.domain.model.TipoCambio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TipoCambioRepository {

    Optional<TipoCambio> buscarMasReciente(String base, String destino);

    List<TipoCambio> buscarHistorico(String base, String destino, LocalDate desde, LocalDate hasta);

    void guardarOActualizar(TipoCambio tipoCambio);

    void guardarOActualizarTodos(List<TipoCambio> tiposCambio);
}
