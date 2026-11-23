package backend.persistence.crud;

import backend.persistence.entity.DiaInhabilEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaInhabilJpaRepository extends JpaRepository<DiaInhabilEntity, Long> {

    List<DiaInhabilEntity> findByAnioOrderByFechaAscTipoAsc(int anio);

    @Query("SELECT MAX(d.actualizadoEn) FROM DiaInhabilEntity d WHERE d.anio = :anio")
    Optional<Instant> ultimaActualizacionPorAnio(@Param("anio") int anio);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO dia_inhabil (fecha, nombre, tipo, anio, fuente, actualizado_en)
            VALUES (:fecha, :nombre, :tipo, :anio, :fuente, :actualizadoEn)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertarSiNoExiste(
            @Param("fecha") LocalDate fecha,
            @Param("nombre") String nombre,
            @Param("tipo") String tipo,
            @Param("anio") int anio,
            @Param("fuente") String fuente,
            @Param("actualizadoEn") Instant actualizadoEn);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE DiaInhabilEntity d
            SET d.nombre = :nombre, d.anio = :anio, d.fuente = :fuente, d.actualizadoEn = :actualizadoEn
            WHERE d.fecha = :fecha AND d.tipo = :tipo
            """)
    int actualizarPorFechaYTipo(
            @Param("fecha") LocalDate fecha,
            @Param("tipo") String tipo,
            @Param("nombre") String nombre,
            @Param("anio") int anio,
            @Param("fuente") String fuente,
            @Param("actualizadoEn") Instant actualizadoEn);
}
