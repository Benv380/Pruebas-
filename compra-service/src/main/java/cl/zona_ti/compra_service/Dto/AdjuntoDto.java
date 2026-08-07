package cl.zona_ti.compra_service.Dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class AdjuntoDto {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdjuntoListadoResponse(
            String success,
            String trace,
            AdjuntoListadoPayload payload,
            List<AdjuntoError> errores) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdjuntoListadoPayload(List<AdjuntoArchivo> files) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdjuntoArchivo(String id, String nombreArchivo) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdjuntoError(String codigo, String mensaje, String detalle) {
    }
}