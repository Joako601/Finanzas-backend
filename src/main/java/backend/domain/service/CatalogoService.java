package backend.domain.service;

import backend.domain.exception.OperacionInvalidaException;
import backend.domain.exception.RecursoNoEncontradoException;
import backend.domain.model.ProductoFinanciero;
import backend.domain.repository.ProductoFinancieroRepository;
import backend.domain.validation.ValidadorUrlHttps;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Casos de uso del catálogo público de productos financieros de México. */
@Service
public class CatalogoService {

    private static final int MIN_COMPARACION = 2;
    private static final int MAX_COMPARACION = 5;

    private final ProductoFinancieroRepository repository;

    public CatalogoService(ProductoFinancieroRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProductoFinanciero> listarActivos(String categoria, String busqueda) {
        return repository.listarActivos(normalizarFiltro(categoria), normalizarFiltro(busqueda));
    }

    @Transactional(readOnly = true)
    public ProductoFinanciero obtenerPorId(Long id) {
        return repository.buscarActivoPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto financiero solicitado no existe."));
    }

    @Transactional(readOnly = true)
    public List<ProductoFinanciero> comparar(List<Long> idsSolicitados) {
        if (idsSolicitados == null
                || idsSolicitados.size() < MIN_COMPARACION
                || idsSolicitados.size() > MAX_COMPARACION) {
            throw new OperacionInvalidaException("Debes indicar entre 2 y 5 productos para comparar.");
        }
        if (idsSolicitados.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new OperacionInvalidaException("Todos los identificadores de productos deben ser positivos.");
        }
        List<Long> unicos = idsSolicitados.stream().distinct().toList();
        if (unicos.size() < MIN_COMPARACION) {
            throw new OperacionInvalidaException("Debes indicar al menos 2 productos distintos para comparar.");
        }

        List<ProductoFinanciero> encontrados = repository.buscarActivosPorIds(unicos);
        if (encontrados.size() != unicos.size()) {
            throw new RecursoNoEncontradoException("Uno o más productos seleccionados para comparar no existen.");
        }

        Map<Long, ProductoFinanciero> porId = new LinkedHashMap<>();
        encontrados.forEach(p -> porId.put(p.id(), p));
        return unicos.stream().map(porId::get).toList();
    }

    @Transactional
    public ProductoFinanciero crear(ProductoFinanciero entrada) {
        validarEntrada(entrada);
        Instant ahora = Instant.now();
        ProductoFinanciero nuevo = new ProductoFinanciero(
                null,
                entrada.nombre().trim(),
                entrada.categoria().trim(),
                entrada.entidad().trim(),
                entrada.costoReferenciaPesos(),
                entrada.periodicidad().trim(),
                entrada.tasaAnualReferencia(),
                entrada.detalleReferencia().trim(),
                entrada.fuenteReferencia().trim(),
                normalizarFiltro(entrada.imageUrl()),
                entrada.verificado(),
                true,
                ahora,
                ahora
        );
        return repository.guardar(nuevo);
    }

    /**
     * Actualiza los datos editables de un producto activo.
     *
     * @param verificado nuevo valor de verificación; {@code null} conserva el valor actual
     */
    @Transactional
    public ProductoFinanciero actualizar(Long id, ProductoFinanciero cambios, Boolean verificado) {
        validarEntrada(cambios);
        ProductoFinanciero actual = repository.buscarActivoPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto financiero a actualizar no existe."));
        ProductoFinanciero actualizado = new ProductoFinanciero(
                actual.id(),
                cambios.nombre().trim(),
                cambios.categoria().trim(),
                cambios.entidad().trim(),
                cambios.costoReferenciaPesos(),
                cambios.periodicidad().trim(),
                cambios.tasaAnualReferencia(),
                cambios.detalleReferencia().trim(),
                cambios.fuenteReferencia().trim(),
                normalizarFiltro(cambios.imageUrl()),
                verificado != null ? verificado : actual.verificado(),
                actual.activo(),
                actual.creadoEn(),
                Instant.now()
        );
        return repository.guardar(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        ProductoFinanciero actual = repository.buscarActivoPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto financiero a eliminar no existe."));
        ProductoFinanciero desactivado = new ProductoFinanciero(
                actual.id(),
                actual.nombre(),
                actual.categoria(),
                actual.entidad(),
                actual.costoReferenciaPesos(),
                actual.periodicidad(),
                actual.tasaAnualReferencia(),
                actual.detalleReferencia(),
                actual.fuenteReferencia(),
                actual.imageUrl(),
                actual.verificado(),
                false,
                actual.creadoEn(),
                Instant.now()
        );
        repository.guardar(desactivado);
    }

    private static void validarEntrada(ProductoFinanciero entrada) {
        if (entrada == null
                || esBlanco(entrada.nombre())
                || esBlanco(entrada.categoria())
                || esBlanco(entrada.entidad())
                || esBlanco(entrada.periodicidad())
                || esBlanco(entrada.detalleReferencia())
                || esBlanco(entrada.fuenteReferencia())
                || entrada.costoReferenciaPesos() < 0) {
            throw new OperacionInvalidaException("Los datos del producto financiero son inválidos.");
        }
        if (!ValidadorUrlHttps.esValidaOOpcional(entrada.imageUrl())) {
            throw new OperacionInvalidaException("La URL de imagen debe ser una dirección HTTPS pública válida.");
        }
    }

    private static boolean esBlanco(String texto) {
        return texto == null || texto.isBlank();
    }

    private static String normalizarFiltro(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
