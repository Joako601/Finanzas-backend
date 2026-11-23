package backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "producto_financiero")
public class ProductoFinancieroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String categoria;

    @Column(nullable = false, length = 80)
    private String entidad;

    @Column(name = "costo_referencia_pesos", nullable = false)
    private long costoReferenciaPesos;

    @Column(nullable = false, length = 40)
    private String periodicidad;

    @Column(name = "tasa_anual_referencia", precision = 6, scale = 2)
    private BigDecimal tasaAnualReferencia;

    @Column(name = "detalle_referencia", nullable = false, length = 280)
    private String detalleReferencia;

    @Column(name = "fuente_referencia", nullable = false, length = 160)
    private String fuenteReferencia;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "activo_unico")
    private Boolean activoUnico;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public long getCostoReferenciaPesos() {
        return costoReferenciaPesos;
    }

    public void setCostoReferenciaPesos(long costoReferenciaPesos) {
        this.costoReferenciaPesos = costoReferenciaPesos;
    }

    public String getPeriodicidad() {
        return periodicidad;
    }

    public void setPeriodicidad(String periodicidad) {
        this.periodicidad = periodicidad;
    }

    public BigDecimal getTasaAnualReferencia() {
        return tasaAnualReferencia;
    }

    public void setTasaAnualReferencia(BigDecimal tasaAnualReferencia) {
        this.tasaAnualReferencia = tasaAnualReferencia;
    }

    public String getDetalleReferencia() {
        return detalleReferencia;
    }

    public void setDetalleReferencia(String detalleReferencia) {
        this.detalleReferencia = detalleReferencia;
    }

    public String getFuenteReferencia() {
        return fuenteReferencia;
    }

    public void setFuenteReferencia(String fuenteReferencia) {
        this.fuenteReferencia = fuenteReferencia;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
        this.activoUnico = activo ? Boolean.TRUE : null;
    }

    public Boolean getActivoUnico() {
        return activoUnico;
    }

    public void setActivoUnico(Boolean activoUnico) {
        this.activoUnico = activoUnico;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Instant creadoEn) {
        this.creadoEn = creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(Instant actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
}
