package cl.zona_ti.compra_service.Dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Basado en la Guia de Uso API Compra Agil v2 (v3.0, Mayo 2026) y verificado contra
// la API real: algunos campos vienen en una estructura mas plana que la documentada
// (ej: id_orden_compra al nivel raiz del detalle, no dentro de un objeto orden_compra;
// estado/proveedor_seleccionado planos dentro de cada proveedor_cotizando, no anidados).
// Los nombres de campo se dejan en snake_case, igual que la respuesta real, para
// mapear 1 a 1 sin necesitar @JsonProperty en cada uno.
public class CompraAgilDto {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CompraAgilListadoResponse(
            String success,
            String trace,
            Listado payload,
            List<CompraAgilError> errors) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CompraAgilDetalleResponse(
            String success,
            String trace,
            Detalle payload,
            List<CompraAgilError> errors) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CompraAgilError(
            String codigo,
            String mensaje,
            String detalle) {
    }

    // ---- Listado (payload de GET /v2/compra-agil) ----

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Listado(List<Item> items, Paginacion paginacion) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Paginacion(
            Integer total_paginas,
            Integer numero_pagina,
            Integer tamano_pagina,
            Integer total_resultados) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            String codigo,
            String nombre,
            Estado estado,
            Convocatoria convocatoria,
            List<Documento> documentos,
            Fechas fechas,
            Montos montos,
            Institucion institucion,
            Resumen resumen,
            Motivos motivos,
            Links links) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Montos(String moneda, Double monto_disponible, Double monto_disponible_clp) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Links(String detalle) {
    }

    // ---- Detalle (payload de GET /v2/compra-agil/{codigo}) ----

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Detalle(
            String codigo,
            String nombre,
            String descripcion,
            Estado estado,
            Convocatoria convocatoria,
            Fechas fechas,
            Entrega entrega,
            List<Documento> documentos,
            Presupuesto presupuesto,
            // Indicador confiable de OC emitida (viene al nivel raiz, no en un objeto
            // "orden_compra" como sugiere la doc). Distinto de null = OC emitida.
            Long id_orden_compra,
            Institucion institucion,
            List<ProductoSolicitado> productos_solicitados,
            List<ProveedorCotizando> proveedores_cotizando,
            Resumen resumen,
            Motivos motivos,
            Flags flags) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entrega(String direccion_entrega, Integer plazo_entrega_dias) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Presupuesto(
            String tipo_presupuesto,
            String moneda,
            Double presupuesto_estimado,
            Double monto_disponible,
            Double monto_disponible_clp,
            Double valor_cambio_moneda,
            String fecha_cambio_moneda) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductoSolicitado(
            Object codigo_producto,
            String nombre,
            String descripcion,
            Double cantidad,
            String unidad_medida) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProveedorCotizando(
            Long id_cotizacion,
            String codigo_sucursal_empresa,
            String codigo_empresa,
            Integer es_emt,
            String razon_social,
            String rut_proveedor,
            String descripcion,
            String fecha_vigencia,
            String fecha_creacion,
            Double valor_neto,
            Double total_impuesto,
            Double monto_despacho,
            Double monto_total,
            Integer proveedor_seleccionado,
            String descripcion_cotizacion,
            List<ProductoCotizado> productos_cotizados,
            Integer estado,
            String justificacion_inadmisibilidad,
            Integer estado_por_comprador,
            Integer activo,
            Long id_oc,
            String nombre_impuesto,
            Integer porcentaje_impuesto) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductoCotizado(
            Object codigo_producto,
            String nombre_producto,
            String descripcion,
            Double cantidad,
            Double precio_unitario,
            Double monto_total_producto) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Flags(
            Boolean considera_requisitos_medioambientales,
            Boolean considera_requisitos_impacto_social_economico) {
    }

    // ---- Compartidos entre listado y detalle ----

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Estado(Integer id_estado, String codigo, String glosa) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Convocatoria(Integer estado_convocatoria, String descripcion) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Documento(Object id, String nombre) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Fechas(
            String fecha_publicacion,
            String fecha_cierre,
            String fecha_ultimo_cambio,
            String fecha_cancelacion,
            String fecha_cierre_primer_llamado,
            String fecha_cierre_segundo_llamado) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Institucion(
            String organismo_comprador,
            String rut,
            String unidad_compra,
            Integer region,
            String nombre_region) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resumen(Double multa_sancion, Integer total_ofertas_recibidas, Integer total_demandas) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Motivos(String motivo_cancelacion, String motivo_desierta, String motivo_seleccion) {
    }
}
