package backend.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CatalogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listarYFiltrarProductosSembradosCumpleContratoJson() throws Exception {
        mockMvc.perform(get("/api/v1/productos")
                        .with(jwt().jwt(j -> j.subject("uid-user-1").claim("email", "uno@tecdesoftware.edu.mx"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(12)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("BBVA Azul"))
                .andExpect(jsonPath("$[0].categoria").value("Tarjeta de crédito"))
                .andExpect(jsonPath("$[0].entidad").value("BBVA"))
                .andExpect(jsonPath("$[0].costoReferenciaPesos").value(748))
                .andExpect(jsonPath("$[0].periodicidad").value("Anual"))
                .andExpect(jsonPath("$[0].detalleReferencia").isString())
                .andExpect(jsonPath("$[0].fuenteReferencia").isString());

        mockMvc.perform(get("/api/v1/productos")
                        .param("categoria", "Tarjeta de crédito")
                        .param("q", "Nu México")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Nu Crédito"));
    }

    @Test
    void compararProductosValidaCantidadPositivosDuplicadosYExistencia() throws Exception {
        var userJwt = jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));

        mockMvc.perform(get("/api/v1/productos/comparar").param("ids", "2,1").with(userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(1));

        mockMvc.perform(get("/api/v1/productos/comparar").param("ids", "1").with(userJwt))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/productos/comparar").param("ids", "1,1").with(userJwt))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/productos/comparar").param("ids", "-1,1,2").with(userJwt))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/productos/comparar").param("ids", "1,2,3,4,5,6").with(userJwt))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/productos/comparar").param("ids", "1,9999").with(userJwt))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCrudCompletoYRecreacionTrasSoftDeleteFuncionaSinConflicto() throws Exception {
        var userJwt = jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var adminJwt = jwt().authorities(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN"));

        String cuerpoValido = """
                {
                  "nombre": "Hey Banco Crédito",
                  "categoria": "Tarjeta de crédito",
                  "entidad": "Hey Banco",
                  "costoReferenciaPesos": 0,
                  "periodicidad": "Anual",
                  "tasaAnualReferencia": 58.40,
                  "detalleReferencia": "Sin anualidad · Recompensas en compras",
                  "fuenteReferencia": "Referencia pública Hey Banco",
                  "imageUrl": "https://cdn.ejemplo.mx/hey.png"
                }
                """;

        // Usuario normal bloqueado con 403 en POST, PUT y DELETE
        mockMvc.perform(post("/api/v1/admin/productos")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoValido).with(userJwt))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/admin/productos/1")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoValido).with(userJwt))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/admin/productos/1").with(userJwt))
                .andExpect(status().isForbidden());

        // Admin crea producto -> 201 Created + Location
        String respuestaJson = mockMvc.perform(post("/api/v1/admin/productos")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoValido).with(adminJwt))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", greaterThanOrEqualTo(13)))
                .andExpect(jsonPath("$.nombre").value("Hey Banco Crédito"))
                .andReturn().getResponse().getContentAsString();
        Long idCreado = ((Number) JsonPath.read(respuestaJson, "$.id")).longValue();

        // Admin actualiza -> 200 OK
        String cuerpoActualizado = cuerpoValido.replace("Hey Banco Crédito", "Hey Banco Crédito Pro");
        mockMvc.perform(put("/api/v1/admin/productos/{id}", idCreado)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoActualizado).with(adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Hey Banco Crédito Pro"));

        // Admin desactiva -> 204 No Content y consulta pública devuelve 404
        mockMvc.perform(delete("/api/v1/admin/productos/{id}", idCreado).with(adminJwt))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/productos/{id}", idCreado).with(userJwt))
                .andExpect(status().isNotFound());

        // Volver a crear un producto activo con el mismo (nombre, entidad) tras soft-delete -> 201 Created
        mockMvc.perform(post("/api/v1/admin/productos")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpoActualizado).with(adminJwt))
                .andExpect(status().isCreated());
    }

    @Test
    void crearProductoDuplicadoActivoDevuelve409Conflict() throws Exception {
        var adminJwt = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
        String duplicado = """
                {
                  "nombre": "BBVA Azul",
                  "categoria": "Tarjeta de crédito",
                  "entidad": "BBVA",
                  "costoReferenciaPesos": 748,
                  "periodicidad": "Anual",
                  "detalleReferencia": "Duplicado del seed",
                  "fuenteReferencia": "CONDUSEF"
                }
                """;
        mockMvc.perform(post("/api/v1/admin/productos")
                        .contentType(MediaType.APPLICATION_JSON).content(duplicado).with(adminJwt))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "http://cdn.ejemplo.mx/img.png",
        "javascript:alert(1)",
        "https://",
        "https:///ruta.png",
        "https://user:pass@cdn.ejemplo.mx/img.png",
        "https://localhost/img.png",
        "https://127.0.0.1/img.png",
        "https://169.254.169.254/latest/meta-data",
        "https://cdn.ejemplo.mx/img.png?a=1 b=2"
    })
    void rechazaUrlsDeImagenInsegurasOMalformadasEnPost(String urlInvalida) throws Exception {
        String cuerpo = """
                {
                  "nombre": "Producto Seguro",
                  "categoria": "Servicio",
                  "entidad": "Entidad MX",
                  "costoReferenciaPesos": 100,
                  "periodicidad": "Mensual",
                  "detalleReferencia": "Detalle válido",
                  "fuenteReferencia": "Fuente válida",
                  "imageUrl": "%s"
                }
                """.formatted(urlInvalida);

        mockMvc.perform(post("/api/v1/admin/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.imageUrl").exists());
    }

    @Test
    void rechazaCostoFaltanteOParametrosInvalidosCon400() throws Exception {
        var adminJwt = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
        var userJwt = jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));

        String sinCosto = """
                {
                  "nombre": "Sin Costo",
                  "categoria": "Servicio",
                  "entidad": "Entidad",
                  "periodicidad": "Mensual",
                  "detalleReferencia": "Detalle",
                  "fuenteReferencia": "Fuente"
                }
                """;
        mockMvc.perform(post("/api/v1/admin/productos")
                        .contentType(MediaType.APPLICATION_JSON).content(sinCosto).with(adminJwt))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.costoReferenciaPesos").exists());

        mockMvc.perform(get("/api/v1/productos/0").with(userJwt))
                .andExpect(status().isBadRequest());
    }
}
