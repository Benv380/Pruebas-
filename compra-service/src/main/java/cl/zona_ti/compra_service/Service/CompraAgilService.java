package cl.zona_ti.compra_service.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.stereotype.Service;

import cl.zona_ti.compra_service.Client.CompraAgilClient;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.CompraAgilDetalleResponse;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.CompraAgilListadoResponse;

@Service
public class CompraAgilService {

    private final CompraAgilClient compraAgilClient;

    public CompraAgilService(CompraAgilClient compraAgilClient) {
        this.compraAgilClient = compraAgilClient;
    }

    public CompraAgilListadoResponse listar(Map<String, String> filtros) {
        return compraAgilClient.listar(filtros);
    }

    public CompraAgilDetalleResponse getDetalleByCodigo(String codigo) {
        return compraAgilClient.getDetalleByCodigo(codigo);
    }

    public CompraAgilListadoResponse listarUltimasOchoHoras() {
        // El filtrado por fecha lo hace la API real via publicado_desde/publicado_hasta
        // (ver CompraAgilClient.listar), no hay que traer todo y filtrar en memoria.
        OffsetDateTime hasta = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime desde = hasta.minusHours(8);

        Map<String, String> filtros = Map.of(
                "publicado_desde", desde.format(DateTimeFormatter.ISO_INSTANT),
                "publicado_hasta", hasta.format(DateTimeFormatter.ISO_INSTANT));

        return compraAgilClient.listar(filtros);
    }

}
