package backend.web;

import backend.domain.gateway.BanxicoGateway;
import backend.domain.gateway.FestivosGateway;
import backend.domain.model.DiaInhabil;
import backend.domain.model.IndicadorEconomico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class IndicadoresYCalendarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BanxicoGateway banxicoGateway;

    @MockitoBean
    private FestivosGateway festivosGateway;

    @Test
    void indicadoresSinMetadatosInternosYSimulacionIncluyendoTasaCero() throws Exception {
        var userJwt = jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
        Instant ahora = Instant.now();
        when(banxicoGateway.estaConfigurado()).thenReturn(true);
        when(banxicoGateway.obtenerSerieOportuna(eq("CETES_28"), eq("CETES 28 días"), eq("%"), eq("SF43936")))
                .thenReturn(Optional.of(new IndicadorEconomico(
                        "CETES_28", "CETES 28 días", new BigDecimal("10.000000"), "%",
                        LocalDate.of(2026, 11, 19), "Banxico SIE", "SF43936", ahora, false)));
        when(banxicoGateway.obtenerSerieOportuna(eq("TASA_OBJETIVO"), eq("Tasa objetivo Banxico"), eq("%"), eq("SF61745")))
                .thenReturn(Optional.empty());
        when(banxicoGateway.obtenerSerieOportuna(eq("UDI"), eq("Valor de la UDI"), eq("MXN"), eq("SP68257")))
                .thenReturn(Optional.empty());
        when(banxicoGateway.obtenerSerieOportuna(eq("USD_FIX"), eq("Tipo de cambio FIX (USD/MXN)"), eq("MXN"), eq("SF43718")))
                .thenReturn(Optional.empty());

        // Sin token -> 401
        mockMvc.perform(get("/api/v1/indicadores/actual")).andExpect(status().isUnauthorized());

        // Devuelve indicador fresco (`desdeCache=false`) y NO expone `serieExterna`
        mockMvc.perform(get("/api/v1/indicadores/actual").with(userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clave").value("CETES_28"))
                .andExpect(jsonPath("$[0].desdeCache").value(false))
                .andExpect(jsonPath("$[0].serieExterna").doesNotExist());

        // Simular ahorro sin enviar tasa -> usa CETES 28d (10.00%) desde caché
        String payloadCetes = """
                {"montoInicial": 10000.00, "aporteMensual": 1000.00, "meses": 12}
                """;
        mockMvc.perform(post("/api/v1/indicadores/simular-ahorro")
                        .contentType(MediaType.APPLICATION_JSON).content(payloadCetes).with(userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasaAnualPorcentaje").value(10.0))
                .andExpect(jsonPath("$.totalAportado").value(22000.0))
                .andExpect(jsonPath("$.rendimientoEstimado", greaterThanOrEqualTo(1500.0)));

        // Tasa 0%: sin división por cero ni rendimiento negativo
        String payloadCero = """
                {"montoInicial": 5000.00, "aporteMensual": 500.00, "meses": 6, "tasaAnualPorcentaje": 0.00}
                """;
        mockMvc.perform(post("/api/v1/indicadores/simular-ahorro")
                        .contentType(MediaType.APPLICATION_JSON).content(payloadCero).with(userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAportado").value(8000.0))
                .andExpect(jsonPath("$.rendimientoEstimado").value(0.0))
                .andExpect(jsonPath("$.montoFinal").value(8000.0));

        // Ambos montos en cero -> 400
        String ambosCero = """
                {"montoInicial": 0.00, "aporteMensual": 0.00, "meses": 12}
                """;
        mockMvc.perform(post("/api/v1/indicadores/simular-ahorro")
                        .contentType(MediaType.APPLICATION_JSON).content(ambosCero).with(userJwt))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calendarioMxIncluye2NovCnbv1Oct2030YResuelveCruceDeAnioLimite() throws Exception {
        var userJwt = jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
        Instant ahora = Instant.now();
        when(festivosGateway.obtenerFestivosMexico(anyInt())).thenAnswer(inv -> {
            int anio = inv.getArgument(0);
            if (anio == 2026) {
                return List.of(
                        new DiaInhabil(LocalDate.of(2026, 11, 16), "Revolución Mexicana", "OFICIAL_MX", 2026, "Nager.Date", ahora));
            }
            return List.of(); // Fuerza fallback LFT en otros años (ej. 2030 y 2035)
        });

        // Sin token -> 401
        mockMvc.perform(get("/api/v1/calendario/inhabiles")).andExpect(status().isUnauthorized());

        // 2026-11-02 (lunes Día de Muertos CNBV): inhábil bancario -> siguiente 3 nov, anterior viernes 30 oct
        mockMvc.perform(get("/api/v1/calendario/dia-habil").param("fecha", "2026-11-02").with(userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esDiaHabil").value(false))
                .andExpect(jsonPath("$.siguienteDiaHabil").value("2026-11-03"))
                .andExpect(jsonPath("$.anteriorDiaHabil").value("2026-10-30"));

        // 2030: transmisión del Poder Ejecutivo Federal el 1 de octubre (Art. 74 VII LFT)
        mockMvc.perform(get("/api/v1/calendario/inhabiles").param("anio", "2030").with(userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].fecha", hasItem("2030-10-01")))
                .andExpect(jsonPath("$[0].fuente").doesNotExist());

        // Borde 2035-12-30 (domingo) -> 31 dic lunes es hábil; 2035-12-31 es lunes hábil, probemos 2034-12-31/2035-12-25 y 2035-12-29
        // En 2035: 31 dic 2035 es lunes (hábil), pero 2035-12-30 es domingo -> siguiente 31 dic 2035.
        // Probemos cruce directo hacia 2036-01-01 (martes Año Nuevo): 2035-12-29 es sábado, y si 2033-12-31 es sábado -> 2034-01-01 es domingo -> 2034-01-02 lunes hábil.
        mockMvc.perform(get("/api/v1/calendario/dia-habil").param("fecha", "2033-12-31").with(userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esDiaHabil").value(false))
                .andExpect(jsonPath("$.siguienteDiaHabil").value("2034-01-02"));
    }
}
