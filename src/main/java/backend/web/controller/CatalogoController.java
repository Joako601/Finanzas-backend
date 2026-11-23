package backend.web.controller;

import backend.domain.service.CatalogoService;
import backend.web.config.OpenApiConfig;
import backend.web.dto.ProductoFinancieroResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Catálogo financiero", description = "Consulta y comparación de productos financieros de México")
@SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos financieros activos",
            description = "Permite filtrar opcionalmente por categoría exacta y búsqueda por texto.")
    public ResponseEntity<List<ProductoFinancieroResponse>> listar(
            @RequestParam(required = false) @Size(max = 60) String categoria,
            @RequestParam(required = false) @Size(max = 100) String q) {
        List<ProductoFinancieroResponse> respuesta = catalogoService.listarActivos(categoria, q)
                .stream()
                .map(ProductoFinancieroResponse::desde)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un producto financiero por ID")
    public ResponseEntity<ProductoFinancieroResponse> obtenerPorId(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ProductoFinancieroResponse.desde(catalogoService.obtenerPorId(id)));
    }

    @GetMapping("/comparar")
    @Operation(summary = "Comparar de 2 a 5 productos financieros lado a lado")
    public ResponseEntity<List<ProductoFinancieroResponse>> comparar(
            @RequestParam @Size(min = 2, max = 5) List<@NotNull @Positive Long> ids) {
        List<ProductoFinancieroResponse> respuesta = catalogoService.comparar(ids)
                .stream()
                .map(ProductoFinancieroResponse::desde)
                .toList();
        return ResponseEntity.ok(respuesta);
    }
}
