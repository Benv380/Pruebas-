package cl.zona_ti.compra_service.Dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Formato de respuesta para adjuntos de licitaciones (scraping), calcado del
 * de AdjuntoDto (Compra Ágil) para que el frontend pueda tratarlos igual:
 * payload.files -> [{ id, nombreArchivo }].
 */
public class AdjuntoLicitacionDto {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdjuntoLicitacionListadoResponse(
            String success,
            AdjuntoLicitacionListadoPayload payload) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdjuntoLicitacionListadoPayload(List<AdjuntoLicitacionArchivo> files) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdjuntoLicitacionArchivo(String id, String nombreArchivo) {
    }
}
