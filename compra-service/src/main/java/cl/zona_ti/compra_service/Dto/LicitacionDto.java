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

    // Solo se declaran los campos que efectivamente se usan/guardan; el resto
    // de lo que trae Mercado Publico (Etapas, TomaRazon, JustificacionPublicidad,
    // CodigoBIP, etc.) se sigue ignorando a proposito via @JsonIgnoreProperties.
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
            @JsonProperty("DiasCierreLicitacion") String diasCierreLicitacion,
            @JsonProperty("Modalidad") Integer modalidad,
            @JsonProperty("TipoPago") String tipoPago,
            @JsonProperty("Tiempo") String tiempo,
            @JsonProperty("UnidadTiempo") String unidadTiempo,
            @JsonProperty("TiempoDuracionContrato") String tiempoDuracionContrato,
            @JsonProperty("UnidadTiempoDuracionContrato") Integer unidadTiempoDuracionContrato,
            @JsonProperty("EsRenovable") Integer esRenovable,
            @JsonProperty("FuenteFinanciamiento") String fuenteFinanciamiento,
            @JsonProperty("NombreResponsablePago") String nombreResponsablePago,
            @JsonProperty("EmailResponsablePago") String emailResponsablePago,
            @JsonProperty("NombreResponsableContrato") String nombreResponsableContrato,
            @JsonProperty("EmailResponsableContrato") String emailResponsableContrato,
            @JsonProperty("FonoResponsableContrato") String fonoResponsableContrato,
            @JsonProperty("Comprador") Comprador comprador,
            @JsonProperty("Fechas") Fechas fechas,
            @JsonProperty("Items") Items items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Comprador(
            @JsonProperty("CodigoOrganismo") String codigoOrganismo,
            @JsonProperty("NombreOrganismo") String nombreOrganismo,
            @JsonProperty("RutUnidad") String rutUnidad,
            @JsonProperty("CodigoUnidad") String codigoUnidad,
            @JsonProperty("NombreUnidad") String nombreUnidad,
            @JsonProperty("DireccionUnidad") String direccionUnidad,
            @JsonProperty("ComunaUnidad") String comunaUnidad,
            @JsonProperty("RegionUnidad") String regionUnidad,
            @JsonProperty("RutUsuario") String rutUsuario,
            @JsonProperty("CodigoUsuario") String codigoUsuario,
            @JsonProperty("NombreUsuario") String nombreUsuario,
            @JsonProperty("CargoUsuario") String cargoUsuario) {
    }

    // FechasUsuario no se mapea: es un listado libre que llena el comprador y
    // en la practica casi siempre viene null.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Fechas(
            @JsonProperty("FechaCreacion") String fechaCreacion,
            @JsonProperty("FechaPublicacion") String fechaPublicacion,
            @JsonProperty("FechaCierre") String fechaCierre,
            @JsonProperty("FechaInicio") String fechaInicio,
            @JsonProperty("FechaFinal") String fechaFinal,
            @JsonProperty("FechaPubRespuestas") String fechaPubRespuestas,
            @JsonProperty("FechaActoAperturaTecnica") String fechaActoAperturaTecnica,
            @JsonProperty("FechaActoAperturaEconomica") String fechaActoAperturaEconomica,
            @JsonProperty("FechaVisitaTerreno") String fechaVisitaTerreno,
            @JsonProperty("FechaEntregaAntecedentes") String fechaEntregaAntecedentes,
            @JsonProperty("FechaEstimadaAdjudicacion") String fechaEstimadaAdjudicacion,
            @JsonProperty("FechaAdjudicacion") String fechaAdjudicacion,
            @JsonProperty("FechaEstimadaFirma") String fechaEstimadaFirma,
            @JsonProperty("FechaSoporteFisico") String fechaSoporteFisico) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Items(
            @JsonProperty("Cantidad") Integer cantidad,
            @JsonProperty("Listado") List<Item> listado) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            @JsonProperty("Correlativo") Integer correlativo,
            @JsonProperty("CodigoProducto") Long codigoProducto,
            @JsonProperty("CodigoCategoria") String codigoCategoria,
            @JsonProperty("Categoria") String categoria,
            @JsonProperty("NombreProducto") String nombreProducto,
            @JsonProperty("Descripcion") String descripcion,
            @JsonProperty("UnidadMedida") String unidadMedida,
            @JsonProperty("Cantidad") Double cantidad) {
    }
}
