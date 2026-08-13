package cl.zona_ti.compra_service.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.zona_ti.compra_service.Dto.CompraAgilDto.Convocatoria;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.Detalle;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.Documento;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.Entrega;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.Estado;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.Fechas;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.Flags;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.Institucion;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.Item;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.Links;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.Montos;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.Motivos;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.Presupuesto;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.ProductoCotizado;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.ProductoSolicitado;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.ProveedorCotizando;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.Resumen;
import cl.zona_ti.compra_service.Model.CompraAgilDocumentoEntity;
import cl.zona_ti.compra_service.Model.CompraAgilEntity;
import cl.zona_ti.compra_service.Model.CompraAgilProductoCotizadoEntity;
import cl.zona_ti.compra_service.Model.CompraAgilProductoSolicitadoEntity;
import cl.zona_ti.compra_service.Model.CompraAgilProveedorCotizandoEntity;
import cl.zona_ti.compra_service.Util.FechaParser;

// Convierte entre los DTO de la API Compra Agil (Item = listado resumido,
// Detalle = detalle completo) y CompraAgilEntity. El listado NO trae varios
// campos que solo entrega el detalle (descripcion, entrega, productos
// solicitados, proveedores cotizando, id_orden_compra, flags...), por eso
// mergeFromItem() solo pisa las columnas que el listado realmente trae y deja
// el resto tal como estaba en cache si ya existia una fila para ese codigo.
@Component
public class CompraAgilMapper {

    public CompraAgilEntity mergeFromItem(CompraAgilEntity existente, Item item, LocalDateTime ahora) {
        CompraAgilEntity entity = existente != null ? existente : new CompraAgilEntity();
        entity.setCodigo(item.codigo());
        entity.setNombre(item.nombre());
        aplicarEstado(entity, item.estado());
        aplicarConvocatoria(entity, item.convocatoria());
        aplicarFechas(entity, item.fechas());
        aplicarMontos(entity, item.montos());
        aplicarInstitucion(entity, item.institucion());
        aplicarResumen(entity, item.resumen());
        aplicarMotivos(entity, item.motivos());
        entity.setFechaSync(ahora);
        sincronizarDocumentos(entity, item.documentos());
        return entity;
    }

    public CompraAgilEntity toEntity(Detalle detalle, CompraAgilEntity existente, LocalDateTime ahora) {
        CompraAgilEntity entity = existente != null ? existente : new CompraAgilEntity();
        entity.setCodigo(detalle.codigo());
        entity.setNombre(detalle.nombre());
        entity.setDescripcion(detalle.descripcion());
        aplicarEstado(entity, detalle.estado());
        aplicarConvocatoria(entity, detalle.convocatoria());
        aplicarFechas(entity, detalle.fechas());

        Entrega entrega = detalle.entrega();
        if (entrega != null) {
            entity.setDireccionEntrega(entrega.direccion_entrega());
            entity.setPlazoEntregaDias(entrega.plazo_entrega_dias());
        }

        Presupuesto presupuesto = detalle.presupuesto();
        if (presupuesto != null) {
            entity.setTipoPresupuesto(presupuesto.tipo_presupuesto());
            entity.setMoneda(presupuesto.moneda());
            entity.setPresupuestoEstimado(bd(presupuesto.presupuesto_estimado()));
            entity.setMontoDisponible(bd(presupuesto.monto_disponible()));
            entity.setMontoDisponibleClp(bd(presupuesto.monto_disponible_clp()));
            entity.setValorCambioMoneda(bd(presupuesto.valor_cambio_moneda()));
            entity.setFechaCambioMoneda(presupuesto.fecha_cambio_moneda());
        }

        entity.setIdOrdenCompra(detalle.id_orden_compra());
        aplicarInstitucion(entity, detalle.institucion());
        aplicarResumen(entity, detalle.resumen());
        aplicarMotivos(entity, detalle.motivos());

        Flags flags = detalle.flags();
        if (flags != null) {
            entity.setConsideraRequisitosMedioambientales(flags.considera_requisitos_medioambientales());
            entity.setConsideraRequisitosImpactoSocial(flags.considera_requisitos_impacto_social_economico());
        }

        entity.setDetalleCompleto(true);
        entity.setFechaSync(ahora);

        sincronizarDocumentos(entity, detalle.documentos());
        sincronizarProductosSolicitados(entity, detalle.productos_solicitados());
        sincronizarProveedoresCotizando(entity, detalle.proveedores_cotizando());

        return entity;
    }

    public Item toItemDto(CompraAgilEntity entity) {
        return new Item(
                entity.getCodigo(),
                entity.getNombre(),
                estadoDto(entity),
                convocatoriaDto(entity),
                documentosDto(entity),
                fechasDto(entity),
                new Montos(entity.getMoneda(), db(entity.getMontoDisponible()), db(entity.getMontoDisponibleClp())),
                institucionDto(entity),
                resumenDto(entity),
                motivosDto(entity),
                (Links) null);
    }

    public Detalle toDetalleDto(CompraAgilEntity entity) {
        return new Detalle(
                entity.getCodigo(),
                entity.getNombre(),
                entity.getDescripcion(),
                estadoDto(entity),
                convocatoriaDto(entity),
                fechasDto(entity),
                new Entrega(entity.getDireccionEntrega(), entity.getPlazoEntregaDias()),
                documentosDto(entity),
                new Presupuesto(
                        entity.getTipoPresupuesto(),
                        entity.getMoneda(),
                        db(entity.getPresupuestoEstimado()),
                        db(entity.getMontoDisponible()),
                        db(entity.getMontoDisponibleClp()),
                        db(entity.getValorCambioMoneda()),
                        entity.getFechaCambioMoneda()),
                entity.getIdOrdenCompra(),
                institucionDto(entity),
                entity.getProductosSolicitados().stream().map(this::productoSolicitadoDto).toList(),
                entity.getProveedoresCotizando().stream().map(this::proveedorCotizandoDto).toList(),
                resumenDto(entity),
                motivosDto(entity),
                new Flags(entity.getConsideraRequisitosMedioambientales(), entity.getConsideraRequisitosImpactoSocial()));
    }

    // ---- helpers de aplicacion (DTO -> entity) ----

    private void aplicarEstado(CompraAgilEntity entity, Estado estado) {
        if (estado == null) {
            return;
        }
        entity.setIdEstado(estado.id_estado());
        entity.setEstadoCodigo(estado.codigo());
        entity.setEstadoGlosa(estado.glosa());
    }

    private void aplicarConvocatoria(CompraAgilEntity entity, Convocatoria convocatoria) {
        if (convocatoria == null) {
            return;
        }
        entity.setConvocatoriaEstado(convocatoria.estado_convocatoria());
        entity.setConvocatoriaDescripcion(convocatoria.descripcion());
    }

    private void aplicarFechas(CompraAgilEntity entity, Fechas fechas) {
        if (fechas == null) {
            return;
        }
        entity.setFechaPublicacion(FechaParser.parse(fechas.fecha_publicacion()));
        entity.setFechaCierre(FechaParser.parse(fechas.fecha_cierre()));
        entity.setFechaUltimoCambio(FechaParser.parse(fechas.fecha_ultimo_cambio()));
        entity.setFechaCancelacion(FechaParser.parse(fechas.fecha_cancelacion()));
        entity.setFechaCierrePrimerLlamado(FechaParser.parse(fechas.fecha_cierre_primer_llamado()));
        entity.setFechaCierreSegundoLlamado(FechaParser.parse(fechas.fecha_cierre_segundo_llamado()));
    }

    private void aplicarMontos(CompraAgilEntity entity, Montos montos) {
        if (montos == null) {
            return;
        }
        entity.setMoneda(montos.moneda());
        entity.setMontoDisponible(bd(montos.monto_disponible()));
        entity.setMontoDisponibleClp(bd(montos.monto_disponible_clp()));
    }

    private void aplicarInstitucion(CompraAgilEntity entity, Institucion institucion) {
        if (institucion == null) {
            return;
        }
        entity.setOrganismoComprador(institucion.organismo_comprador());
        entity.setRutInstitucion(institucion.rut());
        entity.setUnidadCompra(institucion.unidad_compra());
        entity.setRegion(institucion.region());
        entity.setNombreRegion(institucion.nombre_region());
    }

    private void aplicarResumen(CompraAgilEntity entity, Resumen resumen) {
        if (resumen == null) {
            return;
        }
        entity.setMultaSancion(bd(resumen.multa_sancion()));
        entity.setTotalOfertasRecibidas(resumen.total_ofertas_recibidas());
        entity.setTotalDemandas(resumen.total_demandas());
    }

    private void aplicarMotivos(CompraAgilEntity entity, Motivos motivos) {
        if (motivos == null) {
            return;
        }
        entity.setMotivoCancelacion(motivos.motivo_cancelacion());
        entity.setMotivoDesierta(motivos.motivo_desierta());
        entity.setMotivoSeleccion(motivos.motivo_seleccion());
    }

    // Documentos y productos solicitados no tienen clave natural propia (la API
    // no expone un id estable para ellos aparte del codigo_producto, que puede
    // repetirse), asi que se resincronizan enteros: se limpia la coleccion
    // existente (Hibernate borra los huerfanos por orphanRemoval) y se cargan de
    // nuevo con los datos que acaban de llegar.
    private void sincronizarDocumentos(CompraAgilEntity entity, List<Documento> documentos) {
        if (documentos == null) {
            return;
        }
        entity.getDocumentos().clear();
        for (Documento doc : documentos) {
            CompraAgilDocumentoEntity docEntity = new CompraAgilDocumentoEntity();
            docEntity.setCompraAgil(entity);
            docEntity.setIdExterno(doc.id() != null ? doc.id().toString() : null);
            docEntity.setNombre(doc.nombre());
            entity.getDocumentos().add(docEntity);
        }
    }

    private void sincronizarProductosSolicitados(CompraAgilEntity entity, List<ProductoSolicitado> productos) {
        if (productos == null) {
            return;
        }
        entity.getProductosSolicitados().clear();
        for (ProductoSolicitado producto : productos) {
            CompraAgilProductoSolicitadoEntity productoEntity = new CompraAgilProductoSolicitadoEntity();
            productoEntity.setCompraAgil(entity);
            productoEntity.setCodigoProducto(producto.codigo_producto() != null ? producto.codigo_producto().toString() : null);
            productoEntity.setNombre(producto.nombre());
            productoEntity.setDescripcion(producto.descripcion());
            productoEntity.setCantidad(bd(producto.cantidad()));
            productoEntity.setUnidadMedida(producto.unidad_medida());
            entity.getProductosSolicitados().add(productoEntity);
        }
    }

    // Los proveedores cotizando SI tienen clave natural (id_cotizacion), asi que
    // en vez de borrar y recrear todo (lo que perderia el auto-id de sus
    // productos cotizados en cada refresco) se actualiza en el lugar la fila que
    // ya exista y solo se agregan/eliminan las que corresponda.
    private void sincronizarProveedoresCotizando(CompraAgilEntity entity, List<ProveedorCotizando> proveedores) {
        if (proveedores == null) {
            return;
        }
        Map<Long, CompraAgilProveedorCotizandoEntity> existentesPorId = new HashMap<>();
        for (CompraAgilProveedorCotizandoEntity existente : entity.getProveedoresCotizando()) {
            existentesPorId.put(existente.getIdCotizacion(), existente);
        }

        List<CompraAgilProveedorCotizandoEntity> resultado = new ArrayList<>();
        for (ProveedorCotizando dto : proveedores) {
            CompraAgilProveedorCotizandoEntity proveedorEntity = existentesPorId.remove(dto.id_cotizacion());
            if (proveedorEntity == null) {
                proveedorEntity = new CompraAgilProveedorCotizandoEntity();
                proveedorEntity.setIdCotizacion(dto.id_cotizacion());
                proveedorEntity.setCompraAgil(entity);
            }
            aplicarProveedor(proveedorEntity, dto);
            resultado.add(proveedorEntity);
        }

        // Lo que sigue en existentesPorId ya no vino en la respuesta: se descarta
        // (huerfano real, no un simple refresco de los mismos ids).
        entity.getProveedoresCotizando().clear();
        entity.getProveedoresCotizando().addAll(resultado);
    }

    private void aplicarProveedor(CompraAgilProveedorCotizandoEntity entity, ProveedorCotizando dto) {
        entity.setCodigoEmpresa(dto.codigo_empresa());
        entity.setCodigoSucursalEmpresa(dto.codigo_sucursal_empresa());
        entity.setEsEmt(dto.es_emt());
        entity.setRazonSocial(dto.razon_social());
        entity.setRutProveedor(dto.rut_proveedor());
        entity.setDescripcion(dto.descripcion());
        entity.setFechaVigencia(dto.fecha_vigencia());
        entity.setFechaCreacion(dto.fecha_creacion());
        entity.setValorNeto(bd(dto.valor_neto()));
        entity.setTotalImpuesto(bd(dto.total_impuesto()));
        entity.setMontoDespacho(bd(dto.monto_despacho()));
        entity.setMontoTotal(bd(dto.monto_total()));
        entity.setProveedorSeleccionado(dto.proveedor_seleccionado());
        entity.setDescripcionCotizacion(dto.descripcion_cotizacion());
        entity.setEstado(dto.estado());
        entity.setJustificacionInadmisibilidad(dto.justificacion_inadmisibilidad());
        entity.setEstadoPorComprador(dto.estado_por_comprador());
        entity.setActivo(dto.activo());
        entity.setIdOc(dto.id_oc());
        entity.setNombreImpuesto(dto.nombre_impuesto());
        entity.setPorcentajeImpuesto(dto.porcentaje_impuesto());

        entity.getProductosCotizados().clear();
        List<ProductoCotizado> productosCotizados = dto.productos_cotizados();
        if (productosCotizados != null) {
            for (ProductoCotizado productoDto : productosCotizados) {
                CompraAgilProductoCotizadoEntity productoEntity = new CompraAgilProductoCotizadoEntity();
                productoEntity.setProveedorCotizando(entity);
                productoEntity.setCodigoProducto(productoDto.codigo_producto() != null ? productoDto.codigo_producto().toString() : null);
                productoEntity.setNombreProducto(productoDto.nombre_producto());
                productoEntity.setDescripcion(productoDto.descripcion());
                productoEntity.setCantidad(bd(productoDto.cantidad()));
                productoEntity.setPrecioUnitario(bd(productoDto.precio_unitario()));
                productoEntity.setMontoTotalProducto(bd(productoDto.monto_total_producto()));
                entity.getProductosCotizados().add(productoEntity);
            }
        }
    }

    // ---- helpers de reconstruccion (entity -> DTO) ----

    private Estado estadoDto(CompraAgilEntity entity) {
        return new Estado(entity.getIdEstado(), entity.getEstadoCodigo(), entity.getEstadoGlosa());
    }

    private Convocatoria convocatoriaDto(CompraAgilEntity entity) {
        return new Convocatoria(entity.getConvocatoriaEstado(), entity.getConvocatoriaDescripcion());
    }

    private Fechas fechasDto(CompraAgilEntity entity) {
        return new Fechas(
                FechaParser.format(entity.getFechaPublicacion()),
                FechaParser.format(entity.getFechaCierre()),
                FechaParser.format(entity.getFechaUltimoCambio()),
                FechaParser.format(entity.getFechaCancelacion()),
                FechaParser.format(entity.getFechaCierrePrimerLlamado()),
                FechaParser.format(entity.getFechaCierreSegundoLlamado()));
    }

    private Institucion institucionDto(CompraAgilEntity entity) {
        return new Institucion(
                entity.getOrganismoComprador(),
                entity.getRutInstitucion(),
                entity.getUnidadCompra(),
                entity.getRegion(),
                entity.getNombreRegion());
    }

    private Resumen resumenDto(CompraAgilEntity entity) {
        return new Resumen(db(entity.getMultaSancion()), entity.getTotalOfertasRecibidas(), entity.getTotalDemandas());
    }

    private Motivos motivosDto(CompraAgilEntity entity) {
        return new Motivos(entity.getMotivoCancelacion(), entity.getMotivoDesierta(), entity.getMotivoSeleccion());
    }

    private List<Documento> documentosDto(CompraAgilEntity entity) {
        return entity.getDocumentos().stream()
                .map(d -> new Documento(d.getIdExterno(), d.getNombre()))
                .toList();
    }

    private ProductoSolicitado productoSolicitadoDto(CompraAgilProductoSolicitadoEntity entity) {
        return new ProductoSolicitado(
                entity.getCodigoProducto(),
                entity.getNombre(),
                entity.getDescripcion(),
                db(entity.getCantidad()),
                entity.getUnidadMedida());
    }

    private ProveedorCotizando proveedorCotizandoDto(CompraAgilProveedorCotizandoEntity entity) {
        return new ProveedorCotizando(
                entity.getIdCotizacion(),
                entity.getCodigoSucursalEmpresa(),
                entity.getCodigoEmpresa(),
                entity.getEsEmt(),
                entity.getRazonSocial(),
                entity.getRutProveedor(),
                entity.getDescripcion(),
                entity.getFechaVigencia(),
                entity.getFechaCreacion(),
                db(entity.getValorNeto()),
                db(entity.getTotalImpuesto()),
                db(entity.getMontoDespacho()),
                db(entity.getMontoTotal()),
                entity.getProveedorSeleccionado(),
                entity.getDescripcionCotizacion(),
                entity.getProductosCotizados().stream().map(this::productoCotizadoDto).toList(),
                entity.getEstado(),
                entity.getJustificacionInadmisibilidad(),
                entity.getEstadoPorComprador(),
                entity.getActivo(),
                entity.getIdOc(),
                entity.getNombreImpuesto(),
                entity.getPorcentajeImpuesto());
    }

    private ProductoCotizado productoCotizadoDto(CompraAgilProductoCotizadoEntity entity) {
        return new ProductoCotizado(
                entity.getCodigoProducto(),
                entity.getNombreProducto(),
                entity.getDescripcion(),
                db(entity.getCantidad()),
                db(entity.getPrecioUnitario()),
                db(entity.getMontoTotalProducto()));
    }

    private static BigDecimal bd(Double valor) {
        return valor != null ? BigDecimal.valueOf(valor) : null;
    }

    private static Double db(BigDecimal valor) {
        return valor != null ? valor.doubleValue() : null;
    }
}
