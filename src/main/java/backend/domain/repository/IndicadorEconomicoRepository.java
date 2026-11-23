package backend.domain.repository;

import backend.domain.model.IndicadorEconomico;

import java.util.List;
import java.util.Optional;

public interface IndicadorEconomicoRepository {

    Optional<IndicadorEconomico> buscarMasReciente(String clave);

    List<IndicadorEconomico> listarUltimos();

    void guardarOActualizar(IndicadorEconomico indicador);
}
