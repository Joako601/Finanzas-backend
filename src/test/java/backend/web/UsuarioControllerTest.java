package backend.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void perfilYFavoritosEstanAisladosAntiIdorYRespetanBajaLogica() throws Exception {
        var jwtUsuarioA = jwt()
                .jwt(j -> j.subject("uid-alice").claim("email", "alice@tecdesoftware.edu.mx"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var jwtUsuarioB = jwt()
                .jwt(j -> j.subject("uid-bob").claim("email", "bob@tecdesoftware.edu.mx"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var jwtAdmin = jwt()
                .jwt(j -> j.subject("uid-admin").claim("email", "admin@tecdesoftware.edu.mx"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"));

        // Sin token -> 401
        mockMvc.perform(get("/api/v1/usuarios/me")).andExpect(status().isUnauthorized());

        // Alice consulta su perfil (upsert atómico)
        mockMvc.perform(get("/api/v1/usuarios/me").with(jwtUsuarioA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("uid-alice"))
                .andExpect(jsonPath("$.correo").value("alice@tecdesoftware.edu.mx"))
                .andExpect(jsonPath("$.rol").value("USER"));

        // Alice agrega producto 2 como favorito dos veces (idempotente)
        mockMvc.perform(post("/api/v1/usuarios/me/favoritos/2").with(jwtUsuarioA))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/usuarios/me/favoritos/2").with(jwtUsuarioA))
                .andExpect(status().isNoContent());

        // Bob NO ve el favorito de Alice y si intenta borrarlo no afecta a Alice (anti-IDOR)
        mockMvc.perform(get("/api/v1/usuarios/me/favoritos").with(jwtUsuarioB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(delete("/api/v1/usuarios/me/favoritos/2").with(jwtUsuarioB))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/usuarios/me/favoritos").with(jwtUsuarioA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(2));

        // Si el admin desactiva el producto 2, desaparece de los favoritos de Alice y no se puede volver a marcar
        mockMvc.perform(delete("/api/v1/admin/productos/2").with(jwtAdmin))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/usuarios/me/favoritos").with(jwtUsuarioA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(post("/api/v1/usuarios/me/favoritos/2").with(jwtUsuarioA))
                .andExpect(status().isNotFound());

        // Producto inexistente o ID negativo
        mockMvc.perform(post("/api/v1/usuarios/me/favoritos/9999").with(jwtUsuarioA))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/usuarios/me/favoritos/-1").with(jwtUsuarioA))
                .andExpect(status().isBadRequest());
    }
}
