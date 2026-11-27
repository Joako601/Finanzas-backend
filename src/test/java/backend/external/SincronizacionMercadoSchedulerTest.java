package backend.external;

import backend.domain.service.CalendarioService;
import backend.domain.service.DivisasService;
import backend.domain.service.IndicadoresService;
import backend.web.config.SchedulingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** Programación de {@link SincronizacionMercadoScheduler} según `finanzas.scheduling.*`. */
class SincronizacionMercadoSchedulerTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(DivisasService.class, () -> mock(DivisasService.class))
            .withBean(IndicadoresService.class, () -> mock(IndicadoresService.class))
            .withBean(CalendarioService.class, () -> mock(CalendarioService.class))
            .withUserConfiguration(SchedulingConfig.class, SincronizacionMercadoScheduler.class);

    @Test
    void respetaLaExpresionCronConfigurada() {
        runner.withPropertyValues(
                        "finanzas.scheduling.enabled=true",
                        "finanzas.scheduling.mercado-cron=0 30 9 * * MON-FRI")
                .run(ctx -> assertThat(expresionMercado(ctx.getBean(ScheduledTaskHolder.class)))
                        .isEqualTo("0 30 9 * * MON-FRI"));
    }

    @Test
    void sinPropiedadUsaUnaVezAlDiaALas13() {
        runner.withPropertyValues("finanzas.scheduling.enabled=true")
                .run(ctx -> assertThat(expresionMercado(ctx.getBean(ScheduledTaskHolder.class)))
                        .isEqualTo(SincronizacionMercadoScheduler.CRON_MERCADO_DEFECTO)
                        .isEqualTo("0 0 13 * * MON-FRI"));
    }

    @Test
    void conservaLaTareaMensualDeCalendario() {
        runner.withPropertyValues("finanzas.scheduling.enabled=true")
                .run(ctx -> assertThat(ctx.getBean(ScheduledTaskHolder.class).getScheduledTasks())
                        .map(ScheduledTask::getTask)
                        .filteredOn(t -> t.toString().contains("sincronizarCalendarioAnual"))
                        .singleElement()
                        .isInstanceOfSatisfying(CronTask.class,
                                t -> assertThat(t.getExpression()).isEqualTo("0 0 3 1 * *")));
    }

    @Test
    void conSchedulingDeshabilitadoNoSeRegistranTareas() {
        runner.withPropertyValues("finanzas.scheduling.enabled=false")
                .run(ctx -> {
                    assertThat(ctx).doesNotHaveBean(SchedulingConfig.class);
                    assertThat(ctx.getBeansOfType(ScheduledTaskHolder.class)).isEmpty();
                });
    }

    @Test
    void applicationPropertiesDeclaraElValorPorDefecto() throws Exception {
        Properties props = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));
        assertThat(props.getProperty("finanzas.scheduling.mercado-cron"))
                .isEqualTo(SincronizacionMercadoScheduler.CRON_MERCADO_DEFECTO);
    }

    @Test
    void unFalloEnDivisasNoImpideSincronizarIndicadores() {
        DivisasService divisas = mock(DivisasService.class);
        IndicadoresService indicadores = mock(IndicadoresService.class);
        doThrow(new IllegalStateException("proveedor caído")).when(divisas).sincronizarParesPrincipales();

        new SincronizacionMercadoScheduler(divisas, indicadores, mock(CalendarioService.class)).sincronizarMercado();

        verify(indicadores).sincronizarBanxico();
    }

    private static String expresionMercado(ScheduledTaskHolder holder) {
        return holder.getScheduledTasks().stream()
                .map(ScheduledTask::getTask)
                .filter(t -> t.toString().contains("sincronizarMercado"))
                .map(t -> ((CronTask) t).getExpression())
                .findFirst()
                .orElseThrow();
    }
}
