package backend.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadatos OpenAPI / Swagger UI con soporte de autenticación Bearer (Firebase ID Token). */
@Configuration
public class OpenApiConfig {

    public static final String ESQUEMA_BEARER = "bearerAuth";

    @Bean
    public OpenAPI finanzasOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finanzas Backend API")
                        .version("0.1.0")
                        .description("API REST para el flavor nube de Finanzas: catálogo financiero de México, "
                                + "divisas, indicadores económicos, calendario bancario y respaldo cifrado Zero-Knowledge."))
                .components(new Components().addSecuritySchemes(
                        ESQUEMA_BEARER,
                        new SecurityScheme()
                                .name(ESQUEMA_BEARER)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Firebase ID Token obtenido con FirebaseAuth en el cliente móvil.")
                ));
    }
}
