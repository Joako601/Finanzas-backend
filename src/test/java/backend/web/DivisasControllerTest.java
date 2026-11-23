package backend.web;

import backend.domain.gateway.BanxicoGateway;
import backend.domain.gateway.DivisasGateway;
import backend.domain.model.TipoCambio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DivisasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DivisasGateway divisasGateway;

    @MockitoBean
    private BanxicoGateway banxicoGateway;

    @Test
    void consultaActualGuardaEnCacheConvierteYNoBloqueaElHistoricoDe30Dias() throws Exception {
        var userJwt = jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
        LocalDate hoy = LocalDate.now();
        when(banxicoGateway.estaConfigurado()).thenReturn(false);
        when(divisasGateway.obtenerActual("USD", "MXN")).thenReturn(Optional.of(
                new TipoCambio("USD", "MXN", hoy,
                        new BigDecimal("19.850000"), "Frankfurter (BCE)", Instant.now(), false)));

        // Sin token -> 401
        mockMvc.perform(get("/api/v1/divisas/actual")).andExpect(status().isUnauthorized());

        // Primera llamada en minúsculas (usd -> mxn): normaliza y consulta proveedor
        mockMvc.perform(get("/api/v1/divisas/actual")
                        .param("base", "usd")
                        .param("destino", "mxn")
                        .with(userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monedaBase").value("USD"))
                .andExpect(jsonPath("$.monedaDestino").value("MXN"))
                .andExpect(jsonPath("$.tasa").value(19.85))
                .andExpect(jsonPath("$.desdeCache").value(false));

        // Conversión: usa caché sin llamar otra vez a obtenerActual
        mockMvc.perform(get("/api/v1/divisas/convertir")
                        .param("monto", "20")
                        .param("de", "USD")
                        .param("a", "MXN")
                        .with(userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoOrigen").value(20.00))
                .andExpect(jsonPath("$.montoDestino").value(397.00))
                .andExpect(jsonPath("$.desdeCache").value(true));

        verify(divisasGateway, times(1)).obtenerActual("USD", "MXN");

        // Pedir histórico de 30 días después de /actual NO debe conformarse con el 1 punto en caché
        when(divisasGateway.obtenerHistorico(eq("USD"), eq("MXN"), any(), any())).thenReturn(List.of(
                new TipoCambio("USD", "MXN", hoy.minusDays(28),
                        new BigDecimal("19.500000"), "Frankfurter (BCE)", Instant.now(), false),
                new TipoCambio("USD", "MXN", hoy.minusDays(14),
                        new BigDecimal("19.700000"), "Frankfurter (BCE)", Instant.now(), false),
                new TipoCambio("USD", "MXN", hoy,
                        new BigDecimal("19.850000"), "Frankfurter (BCE)", Instant.now(), false)));

        mockMvc.perform(get("/api/v1/divisas/historico")
                        .param("base", "USD")
                        .param("destino", "MXN")
                        .param("dias", "30")
                        .with(userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
        verify(divisasGateway, times(1)).obtenerHistorico(eq("USD"), eq("MXN"), any(), any());
    }

    @Test
    void rechazaMontosInvalidosYDevuelve503SinCacheCuandoProveedorFalla() throws Exception {
        var userJwt = jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
        when(banxicoGateway.estaConfigurado()).thenReturn(false);
        when(divisasGateway.obtenerActual("EUR", "MXN")).thenThrow(new RuntimeException("timeout"));

        mockMvc.perform(get("/api/v1/divisas/actual")
                        .param("base", "EUR").param("destino", "MXN").with(userJwt))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));

        // Monto < 0.01 o con > 2 decimales -> 400
        mockMvc.perform(get("/api/v1/divisas/convertir").param("monto", "0.001").with(userJwt))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/divisas/convertir").param("monto", "10.004").with(userJwt))
                .andExpect(status().isBadRequest());

        // Monedas iguales, no soportadas o formato inválido -> 400
        mockMvc.perform(get("/api/v1/divisas/actual").param("base", "MXN").param("destino", "MXN").with(userJwt))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/divisas/actual").param("base", "XXX").param("destino", "MXN").with(userJwt))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/divisas/actual").param("base", "12$").param("destino", "MXN").with(userJwt))
                .andExpect(status().isBadRequest());
    }
}
