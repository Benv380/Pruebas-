package cl.zona_ti.compra_service.Dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LicitacionDto {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LicitacionResponse(
            @JsonProperty("Cantidad") Integer cantidad,
            @JsonProperty("FechaCreacion") String fechaCreacion,
            @JsonProperty("Version") String version,
            @JsonProperty("Listado") List<Licitacion> listado) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Licitacion(
            @JsonProperty("CodigoExterno") String codigoExterno,
            @JsonProperty("Nombre") String nombre,
            @JsonProperty("CodigoEstado") Integer codigoEstado,
            @JsonProperty("Estado") String estado,
            @JsonProperty("Descripcion") String descripcion,
            @JsonProperty("FechaCierre") String fechaCierre,
            @JsonProperty("Moneda") String moneda,
            @JsonProperty("MontoEstimado") Double montoEstimado,
            @JsonProperty("Tipo") String tipo,
            @JsonProperty("Comprador") Comprador comprador,
            @JsonProperty("Fechas") Fechas fechas) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Comprador(
            @JsonProperty("CodigoOrganismo") String codigoOrganismo,
            @JsonProperty("NombreOrganismo") String nombreOrganismo,
            @JsonProperty("NombreUnidad") String nombreUnidad,
            @JsonProperty("RegionUnidad") String regionUnidad,
            @JsonProperty("ComunaUnidad") String comunaUnidad) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Fechas(
            @JsonProperty("FechaPublicacion") String fechaPublicacion,
            @JsonProperty("FechaCierre") String fechaCierre,
            @JsonProperty("FechaAdjudicacion") String fechaAdjudicacion) {
    }
}
