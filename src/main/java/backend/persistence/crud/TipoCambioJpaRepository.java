package backend.persistence.crud;

import backend.persistence.entity.TipoCambioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TipoCambioJpaRepository extends JpaRepository<TipoCambioEntity, Long> {

    Optional<TipoCambioEntity> findFirstByMonedaBaseAndMonedaDestinoOrderByFechaDescActualizadoEnDesc(
            String monedaBase,
            String monedaDestino);

    List<TipoCambioEntity> findByMonedaBaseAndMonedaDestinoAndFechaBetweenOrderByFechaAscActualizadoEnDesc(
            String monedaBase,
            String monedaDestino,
            LocalDate desde,
            LocalDate hasta);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO tipo_cambio (moneda_base, moneda_destino, fecha, tasa, fuente, actualizado_en)
            VALUES (:base, :destino, :fecha, :tasa, :fuente, :actualizadoEn)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertarSiNoExiste(
            @Param("base") String base,
            @Param("destino") String destino,
            @Param("fecha") LocalDate fecha,
            @Param("tasa") BigDecimal tasa,
            @Param("fuente") String fuente,
            @Param("actualizadoEn") Instant actualizadoEn);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE TipoCambioEntity t
            SET t.tasa = :tasa, t.actualizadoEn = :actualizadoEn
            WHERE t.monedaBase = :base AND t.monedaDestino = :destino
              AND t.fecha = :fecha AND t.fuente = :fuente
            """)
    int actualizarTasa(
            @Param("base") String base,
            @Param("destino") String destino,
            @Param("fecha") LocalDate fecha,
            @Param("fuente") String fuente,
            @Param("tasa") BigDecimal tasa,
            @Param("actualizadoEn") Instant actualizadoEn);
}
