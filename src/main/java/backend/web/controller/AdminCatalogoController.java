package backend.web.controller;

import backend.domain.model.ProductoFinanciero;
import backend.domain.service.CatalogoService;
import backend.web.config.OpenApiConfig;
import backend.web.dto.GuardarProductoRequest;
import backend.web.dto.ProductoFinancieroResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Validated
@RestController
@RequestMapping("/api/v1/admin/productos")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administración de catálogo", description = "Alta, edición y baja lógica de productos (rol ADMIN)")
@SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
public class AdminCatalogoController {

    private final CatalogoService catalogoService;

    public AdminCatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo producto financiero",
            description = "El campo opcional `verificado` queda en `false` si se omite.")
    public ResponseEntity<ProductoFinancieroResponse> crear(@Valid @RequestBody GuardarProductoRequest request) {
        ProductoFinanciero creado = catalogoService.crear(request.aDominio());
        URI ubicacion = URI.create("/api/v1/productos/" + creado.id());
        return ResponseEntity.created(ubicacion).body(ProductoFinancieroResponse.desde(creado));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un producto financiero existente",
            description = "Si `verificado` se omite o es `null`, se conserva el valor actual del producto.")
    public ResponseEntity<ProductoFinancieroResponse> actualizar(
            @PathVariable @Positive Long id,
            @Valid @RequestBody GuardarProductoRequest request) {
        ProductoFinanciero actualizado = catalogoService.actualizar(id, request.aDominio(), request.verificado());
        return ResponseEntity.ok(ProductoFinancieroResponse.desde(actualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar (baja lógica) un producto financiero")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) {
        catalogoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
