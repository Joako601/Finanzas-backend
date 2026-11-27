package backend.persistence;

import backend.domain.model.ProductoFinanciero;
import backend.persistence.entity.ProductoFinancieroEntity;
import backend.persistence.mapper.ProductoFinancieroMapper;
import backend.persistence.mapper.ProductoFinancieroMapperImpl;
import backend.web.dto.GuardarProductoRequest;
import backend.web.dto.ProductoFinancieroResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/** Mapeo de {@code verificado} entre dominio, entidad JPA y DTOs web. */
class ProductoFinancieroMapeoTest {

    private final ProductoFinancieroMapper mapper = new ProductoFinancieroMapperImpl();

    private static ProductoFinanciero producto(boolean verificado) {
        Instant t = Instant.parse("2026-11-26T12:00:00Z");
        return new ProductoFinanciero(
                7L, "Tarjeta X", "Tarjeta de crédito", "Banco X", 600L, "Anual",
                new BigDecimal("45.50"), "Detalle", "Fuente", null,
                verificado, true, t, t);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void dominioEntidadDominioConservaVerificado(boolean verificado) {
        ProductoFinancieroEntity entidad = mapper.toEntity(producto(verificado));
        assertThat(entidad.isVerificado()).isEqualTo(verificado);
        assertThat(entidad.getActivoUnico()).isTrue();

        ProductoFinanciero deVuelta = mapper.toDomain(entidad);
        assertThat(deVuelta).isEqualTo(producto(verificado));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void respuestaExponeVerificado(boolean verificado) {
        ProductoFinancieroResponse respuesta = ProductoFinancieroResponse.desde(producto(verificado));
        assertThat(respuesta.verificado()).isEqualTo(verificado);
        assertThat(respuesta.nombre()).isEqualTo("Tarjeta X");
    }

    @Test
    void solicitudSinVerificadoProduceFalseEnDominio() {
        GuardarProductoRequest sinCampo = new GuardarProductoRequest(
                "N", "C", "E", 0L, "Anual", null, "D", "F", null, null);
        GuardarProductoRequest conTrue = new GuardarProductoRequest(
                "N", "C", "E", 0L, "Anual", null, "D", "F", null, true);

        assertThat(sinCampo.aDominio().verificado()).isFalse();
        assertThat(conTrue.aDominio().verificado()).isTrue();
    }
}
