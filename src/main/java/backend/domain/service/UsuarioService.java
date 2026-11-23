package backend.domain.service;

import backend.domain.exception.RecursoNoEncontradoException;
import backend.domain.model.PerfilUsuario;
import backend.domain.model.ProductoFinanciero;
import backend.domain.repository.FavoritoRepository;
import backend.domain.repository.ProductoFinancieroRepository;
import backend.domain.repository.UsuarioPerfilRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Casos de uso del perfil autenticado y sus productos favoritos (siempre aislados por `firebaseUid`). */
@Service
public class UsuarioService {

    private static final int MAX_CORREO_LENGTH = 254;

    private final UsuarioPerfilRepository perfilRepository;
    private final FavoritoRepository favoritoRepository;
    private final ProductoFinancieroRepository productoRepository;

    public UsuarioService(
            UsuarioPerfilRepository perfilRepository,
            FavoritoRepository favoritoRepository,
            ProductoFinancieroRepository productoRepository) {
        this.perfilRepository = perfilRepository;
        this.favoritoRepository = favoritoRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public PerfilUsuario sincronizarPerfil(String firebaseUid, String correo, boolean esAdmin) {
        String rol = esAdmin ? "ADMIN" : "USER";
        String correoSeguro = normalizarCorreo(correo);
        return perfilRepository.sincronizar(firebaseUid, correoSeguro, rol, Instant.now());
    }

    @Transactional(readOnly = true)
    public List<ProductoFinanciero> listarFavoritos(String firebaseUid) {
        return favoritoRepository.listarActivosDeUsuario(firebaseUid);
    }

    @Transactional
    public void agregarFavorito(String firebaseUid, String correo, boolean esAdmin, Long productoId) {
        sincronizarPerfil(firebaseUid, correo, esAdmin);
        productoRepository.buscarActivoPorId(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto financiero solicitado no existe."));
        favoritoRepository.agregarSiNoExiste(firebaseUid, productoId);
    }

    @Transactional
    public void eliminarFavorito(String firebaseUid, Long productoId) {
        favoritoRepository.eliminar(firebaseUid, productoId);
    }

    private static String normalizarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return "sin-correo@firebase.local";
        }
        String limpio = correo.trim();
        return limpio.length() > MAX_CORREO_LENGTH ? limpio.substring(0, MAX_CORREO_LENGTH) : limpio;
    }
}
