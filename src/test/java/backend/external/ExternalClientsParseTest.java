package backend.external;

import backend.domain.model.DiaInhabil;
import backend.domain.model.IndicadorEconomico;
import backend.domain.model.TipoCambio;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Pruebas aisladas de parseo HTTP real contra contratos de Frankfurter, Banxico SIE y Nager.Date. */
class ExternalClientsParseTest {

    @Test
    void frankfurterParseaActualEHistoricoYSinFiltrarTokenDeBanxico() {
        RestClient.Builder builderCompartido = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builderCompartido).build();

        // Instanciar Banxico primero sobre el mismo builder no debe contaminar a Frankfurter gracias a .clone()
        new BanxicoSieClient(builderCompartido, "https://banxico.test/v1", "token-privado");
        FrankfurterDivisasClient client = new FrankfurterDivisasClient(builderCompartido, "https://api.frankfurter.dev/v1");

        server.expect(requestTo("https://api.frankfurter.dev/v1/latest?base=USD&symbols=MXN"))
                .andExpect(headerDoesNotExist("Bmx-Token"))
                .andRespond(withSuccess("""
                        {"amount":1.0,"base":"USD","date":"2026-11-20","rates":{"MXN":19.8725}}
                        """, MediaType.APPLICATION_JSON));

        Optional<TipoCambio> actual = client.obtenerActual("USD", "MXN");
        assertTrue(actual.isPresent());
        assertEquals(new BigDecimal("19.872500"), actual.get().tasa());
        assertEquals(LocalDate.of(2026, 11, 20), actual.get().fecha());

        server.reset();
        server.expect(requestTo("https://api.frankfurter.dev/v1/2026-11-18..2026-11-20?base=USD&symbols=MXN"))
                .andExpect(headerDoesNotExist("Bmx-Token"))
                .andRespond(withSuccess("""
                        {"base":"USD","rates":{"2026-11-20":{"MXN":19.87},"2026-11-18":{"MXN":19.65},"fecha-mala":{"MXN":19.0}}}
                        """, MediaType.APPLICATION_JSON));

        List<TipoCambio> historico = client.obtenerHistorico(
                "USD", "MXN", LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 20));
        assertEquals(2, historico.size());
        assertEquals(LocalDate.of(2026, 11, 18), historico.get(0).fecha());
        assertEquals(LocalDate.of(2026, 11, 20), historico.get(1).fecha());
        server.verify();
    }

    @Test
    void banxicoEnviaHeaderBmxTokenYParseaFechaMxEIgnoraNE() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        BanxicoSieClient client = new BanxicoSieClient(builder, "https://banxico.test/v1", "mi-token-secreto");

        server.expect(requestTo("https://banxico.test/v1/series/SF43936/datos/oportuno"))
                .andExpect(header("Bmx-Token", "mi-token-secreto"))
                .andRespond(withSuccess("""
                        {"bmx":{"series":[{"idSerie":"SF43936","titulo":"Cetes 28","datos":[{"fecha":"19/11/2026","dato":"9.85"}]}]}}
                        """, MediaType.APPLICATION_JSON));

        Optional<IndicadorEconomico> cetes = client.obtenerSerieOportuna("CETES_28", "CETES 28 días", "%", "SF43936");
        assertTrue(cetes.isPresent());
        assertEquals(new BigDecimal("9.850000"), cetes.get().valor());
        assertEquals(LocalDate.of(2026, 11, 19), cetes.get().fecha());

        server.reset();
        server.expect(requestTo("https://banxico.test/v1/series/SF43718/datos/oportuno"))
                .andRespond(withSuccess("""
                        {"bmx":{"series":[{"idSerie":"SF43718","titulo":"FIX","datos":[{"fecha":"22/11/2026","dato":"N/E"}]}]}}
                        """, MediaType.APPLICATION_JSON));

        Optional<IndicadorEconomico> sinDato = client.obtenerSerieOportuna("USD_FIX", "FIX", "MXN", "SF43718");
        assertTrue(sinDato.isEmpty());
        server.verify();
    }

    @Test
    void nagerDateParseaDeduplicaYFiltraAnioCorrecto() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        NagerDateFestivosClient client = new NagerDateFestivosClient(builder, "https://date.nager.at/api/v3");

        server.expect(requestTo("https://date.nager.at/api/v3/PublicHolidays/2026/MX"))
                .andRespond(withSuccess("""
                        [
                          {"date":"2026-01-01","localName":"Año Nuevo","name":"New Year's Day"},
                          {"date":"2026-01-01","localName":"Duplicado","name":"Duplicate"},
                          {"date":"2025-12-25","localName":"Otro año","name":"Other year"},
                          {"date":"2026-09-16","localName":"Día de la Independencia","name":"Independence Day"}
                        ]
                        """, MediaType.APPLICATION_JSON));

        List<DiaInhabil> festivos = client.obtenerFestivosMexico(2026);
        assertEquals(2, festivos.size());
        assertEquals("Año Nuevo", festivos.get(0).nombre());
        assertEquals(LocalDate.of(2026, 9, 16), festivos.get(1).fecha());
        server.verify();
    }
}
