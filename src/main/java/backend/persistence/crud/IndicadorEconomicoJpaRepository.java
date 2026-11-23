package backend.persistence.crud;

import backend.persistence.entity.IndicadorEconomicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IndicadorEconomicoJpaRepository extends JpaRepository<IndicadorEconomicoEntity, Long> {

    Optional<IndicadorEconomicoEntity> findFirstByClaveOrderByFechaDesc(String clave);

    @Query("""
            SELECT i FROM IndicadorEconomicoEntity i
            WHERE i.fecha = (
                SELECT MAX(i2.fecha) FROM IndicadorEconomicoEntity i2 WHERE i2.clave = i.clave
            )
            ORDER BY i.clave ASC
            """)
    List<IndicadorEconomicoEntity> listarUltimosPorClave();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO indicador_economico (clave, nombre, valor, unidad, fecha, fuente, serie_externa, actualizado_en)
            VALUES (:clave, :nombre, :valor, :unidad, :fecha, :fuente, :serieExterna, :actualizadoEn)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertarSiNoExiste(
            @Param("clave") String clave,
            @Param("nombre") String nombre,
            @Param("valor") BigDecimal valor,
            @Param("unidad") String unidad,
            @Param("fecha") LocalDate fecha,
            @Param("fuente") String fuente,
            @Param("serieExterna") String serieExterna,
            @Param("actualizadoEn") Instant actualizadoEn);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE IndicadorEconomicoEntity i
            SET i.nombre = :nombre, i.valor = :valor, i.unidad = :unidad,
                i.fuente = :fuente, i.serieExterna = :serieExterna, i.actualizadoEn = :actualizadoEn
            WHERE i.clave = :clave AND i.fecha = :fecha
            """)
    int actualizarValor(
            @Param("clave") String clave,
            @Param("fecha") LocalDate fecha,
            @Param("nombre") String nombre,
            @Param("valor") BigDecimal valor,
            @Param("unidad") String unidad,
            @Param("fuente") String fuente,
            @Param("serieExterna") String serieExterna,
            @Param("actualizadoEn") Instant actualizadoEn);
}
