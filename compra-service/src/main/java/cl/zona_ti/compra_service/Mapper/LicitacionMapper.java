package cl.zona_ti.compra_service.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import cl.zona_ti.compra_service.Dto.LicitacionDto.Comprador;
import cl.zona_ti.compra_service.Dto.LicitacionDto.Fechas;
import cl.zona_ti.compra_service.Dto.LicitacionDto.Licitacion;
import cl.zona_ti.compra_service.Model.LicitacionEntity;
import cl.zona_ti.compra_service.Util.FechaParser;

@Component
public class LicitacionMapper {

    // El detalle completo (unico que trae "Fechas" y "Comprador", ver comentario
    // en LicitacionService) se guarda entero, reemplazando lo que hubiera en cache.
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

        Comprador comprador = dto.comprador();
        if (comprador != null) {
            entity.setCodigoOrganismo(comprador.codigoOrganismo());
            entity.setNombreOrganismo(comprador.nombreOrganismo());
            entity.setNombreUnidad(comprador.nombreUnidad());
            entity.setRegionUnidad(comprador.regionUnidad());
            entity.setComunaUnidad(comprador.comunaUnidad());
        }

        Fechas fechas = dto.fechas();
        entity.setFechaPublicacion(fechas != null ? FechaParser.parse(fechas.fechaPublicacion()) : null);
        entity.setFechaCierre(FechaParser.parse(fechas != null ? fechas.fechaCierre() : dto.fechaCierre()));
        entity.setFechaAdjudicacion(fechas != null ? FechaParser.parse(fechas.fechaAdjudicacion()) : null);

        entity.setFechaSync(ahora);
        return entity;
    }

    public Licitacion toDto(LicitacionEntity entity) {
        Comprador comprador = new Comprador(
                entity.getCodigoOrganismo(),
                entity.getNombreOrganismo(),
                entity.getNombreUnidad(),
                entity.getRegionUnidad(),
                entity.getComunaUnidad());

        Fechas fechas = new Fechas(
                FechaParser.format(entity.getFechaPublicacion()),
                FechaParser.format(entity.getFechaCierre()),
                FechaParser.format(entity.getFechaAdjudicacion()));

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
                comprador,
                fechas);
    }
}
