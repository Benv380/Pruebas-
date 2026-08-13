package cl.zona_ti.compra_service.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import cl.zona_ti.compra_service.Dto.LicitacionDto.Comprador;
import cl.zona_ti.compra_service.Dto.LicitacionDto.Fechas;
import cl.zona_ti.compra_service.Dto.LicitacionDto.Item;
import cl.zona_ti.compra_service.Dto.LicitacionDto.Items;
import cl.zona_ti.compra_service.Dto.LicitacionDto.Licitacion;
import cl.zona_ti.compra_service.Model.LicitacionEntity;
import cl.zona_ti.compra_service.Model.LicitacionItemEntity;
import cl.zona_ti.compra_service.Util.FechaParser;

@Component
public class LicitacionMapper {

    // El detalle completo (unico que trae "Fechas", "Comprador" e "Items", ver
    // comentario en LicitacionService) se guarda entero, reemplazando lo que
    // hubiera en cache -- incluidos los items, que se recrean desde cero en
    // cada guardado (no tienen clave natural propia, solo el correlativo
    // dentro de la licitacion).
    public LicitacionEntity toEntity(Licitacion dto, LocalDateTime ahora) {
        LicitacionEntity entity = new LicitacionEntity();
        entity.setCodigoExterno(dto.codigoExterno());
        entity.setNombre(dto.nombre());
        entity.setCodigoEstado(dto.codigoEstado());
        entity.setEstado(dto.estado());
        entity.setDescripcion(dto.descripcion());
        entity.setMoneda(dto.moneda());
        entity.setMontoEstimado(dto.montoEstimado() != null ? BigDecimal.valueOf(dto.montoEstimado()) : null);
        entity.setTipo(dto.tipo());
        entity.setDiasCierreLicitacion(dto.diasCierreLicitacion());
        entity.setModalidad(dto.modalidad());
        entity.setTipoPago(dto.tipoPago());
        entity.setTiempo(dto.tiempo());
        entity.setUnidadTiempo(dto.unidadTiempo());
        entity.setTiempoDuracionContrato(dto.tiempoDuracionContrato());
        entity.setUnidadTiempoDuracionContrato(dto.unidadTiempoDuracionContrato());
        entity.setEsRenovable(dto.esRenovable());
        entity.setFuenteFinanciamiento(dto.fuenteFinanciamiento());
        entity.setNombreResponsablePago(dto.nombreResponsablePago());
        entity.setEmailResponsablePago(dto.emailResponsablePago());
        entity.setNombreResponsableContrato(dto.nombreResponsableContrato());
        entity.setEmailResponsableContrato(dto.emailResponsableContrato());
        entity.setFonoResponsableContrato(dto.fonoResponsableContrato());

        Comprador comprador = dto.comprador();
        if (comprador != null) {
            entity.setCodigoOrganismo(comprador.codigoOrganismo());
            entity.setNombreOrganismo(comprador.nombreOrganismo());
            entity.setRutUnidad(comprador.rutUnidad());
            entity.setCodigoUnidad(comprador.codigoUnidad());
            entity.setNombreUnidad(comprador.nombreUnidad());
            entity.setDireccionUnidad(comprador.direccionUnidad());
            entity.setComunaUnidad(comprador.comunaUnidad());
            entity.setRegionUnidad(comprador.regionUnidad());
            entity.setRutUsuario(comprador.rutUsuario());
            entity.setCodigoUsuario(comprador.codigoUsuario());
            entity.setNombreUsuario(comprador.nombreUsuario());
            entity.setCargoUsuario(comprador.cargoUsuario());
        }

        Fechas fechas = dto.fechas();
        entity.setFechaCreacion(fechas != null ? FechaParser.parse(fechas.fechaCreacion()) : null);
        entity.setFechaPublicacion(fechas != null ? FechaParser.parse(fechas.fechaPublicacion()) : null);
        entity.setFechaCierre(FechaParser.parse(fechas != null ? fechas.fechaCierre() : dto.fechaCierre()));
        entity.setFechaInicio(fechas != null ? FechaParser.parse(fechas.fechaInicio()) : null);
        entity.setFechaFinal(fechas != null ? FechaParser.parse(fechas.fechaFinal()) : null);
        entity.setFechaPubRespuestas(fechas != null ? FechaParser.parse(fechas.fechaPubRespuestas()) : null);
        entity.setFechaActoAperturaTecnica(fechas != null ? FechaParser.parse(fechas.fechaActoAperturaTecnica()) : null);
        entity.setFechaActoAperturaEconomica(fechas != null ? FechaParser.parse(fechas.fechaActoAperturaEconomica()) : null);
        entity.setFechaVisitaTerreno(fechas != null ? FechaParser.parse(fechas.fechaVisitaTerreno()) : null);
        entity.setFechaEntregaAntecedentes(fechas != null ? FechaParser.parse(fechas.fechaEntregaAntecedentes()) : null);
        entity.setFechaEstimadaAdjudicacion(fechas != null ? FechaParser.parse(fechas.fechaEstimadaAdjudicacion()) : null);
        entity.setFechaAdjudicacion(fechas != null ? FechaParser.parse(fechas.fechaAdjudicacion()) : null);
        entity.setFechaEstimadaFirma(fechas != null ? FechaParser.parse(fechas.fechaEstimadaFirma()) : null);
        entity.setFechaSoporteFisico(fechas != null ? FechaParser.parse(fechas.fechaSoporteFisico()) : null);

        Items items = dto.items();
        if (items != null && items.listado() != null) {
            for (Item item : items.listado()) {
                LicitacionItemEntity itemEntity = new LicitacionItemEntity();
                itemEntity.setLicitacion(entity);
                itemEntity.setCorrelativo(item.correlativo());
                itemEntity.setCodigoProducto(item.codigoProducto());
                itemEntity.setCodigoCategoria(item.codigoCategoria());
                itemEntity.setCategoria(item.categoria());
                itemEntity.setNombreProducto(item.nombreProducto());
                itemEntity.setDescripcion(item.descripcion());
                itemEntity.setUnidadMedida(item.unidadMedida());
                itemEntity.setCantidad(item.cantidad() != null ? BigDecimal.valueOf(item.cantidad()) : null);
                entity.getItems().add(itemEntity);
            }
        }

        entity.setFechaSync(ahora);
        return entity;
    }

    public Licitacion toDto(LicitacionEntity entity) {
        Comprador comprador = new Comprador(
                entity.getCodigoOrganismo(),
                entity.getNombreOrganismo(),
                entity.getRutUnidad(),
                entity.getCodigoUnidad(),
                entity.getNombreUnidad(),
                entity.getDireccionUnidad(),
                entity.getComunaUnidad(),
                entity.getRegionUnidad(),
                entity.getRutUsuario(),
                entity.getCodigoUsuario(),
                entity.getNombreUsuario(),
                entity.getCargoUsuario());

        Fechas fechas = new Fechas(
                FechaParser.format(entity.getFechaCreacion()),
                FechaParser.format(entity.getFechaPublicacion()),
                FechaParser.format(entity.getFechaCierre()),
                FechaParser.format(entity.getFechaInicio()),
                FechaParser.format(entity.getFechaFinal()),
                FechaParser.format(entity.getFechaPubRespuestas()),
                FechaParser.format(entity.getFechaActoAperturaTecnica()),
                FechaParser.format(entity.getFechaActoAperturaEconomica()),
                FechaParser.format(entity.getFechaVisitaTerreno()),
                FechaParser.format(entity.getFechaEntregaAntecedentes()),
                FechaParser.format(entity.getFechaEstimadaAdjudicacion()),
                FechaParser.format(entity.getFechaAdjudicacion()),
                FechaParser.format(entity.getFechaEstimadaFirma()),
                FechaParser.format(entity.getFechaSoporteFisico()));

        List<Item> itemsDto = new ArrayList<>();
        for (LicitacionItemEntity itemEntity : entity.getItems()) {
            itemsDto.add(new Item(
                    itemEntity.getCorrelativo(),
                    itemEntity.getCodigoProducto(),
                    itemEntity.getCodigoCategoria(),
                    itemEntity.getCategoria(),
                    itemEntity.getNombreProducto(),
                    itemEntity.getDescripcion(),
                    itemEntity.getUnidadMedida(),
                    itemEntity.getCantidad() != null ? itemEntity.getCantidad().doubleValue() : null));
        }
        Items items = new Items(itemsDto.size(), itemsDto);

        return new Licitacion(
                entity.getCodigoExterno(),
                entity.getNombre(),
                entity.getCodigoEstado(),
                entity.getEstado(),
                entity.getDescripcion(),
                FechaParser.format(entity.getFechaCierre()),
                entity.getMoneda(),
                entity.getMontoEstimado() != null ? entity.getMontoEstimado().doubleValue() : null,
                entity.getTipo(),
                entity.getDiasCierreLicitacion(),
                entity.getModalidad(),
                entity.getTipoPago(),
                entity.getTiempo(),
                entity.getUnidadTiempo(),
                entity.getTiempoDuracionContrato(),
                entity.getUnidadTiempoDuracionContrato(),
                entity.getEsRenovable(),
                entity.getFuenteFinanciamiento(),
                entity.getNombreResponsablePago(),
                entity.getEmailResponsablePago(),
                entity.getNombreResponsableContrato(),
                entity.getEmailResponsableContrato(),
                entity.getFonoResponsableContrato(),
                comprador,
                fechas,
                items);
    }
}
