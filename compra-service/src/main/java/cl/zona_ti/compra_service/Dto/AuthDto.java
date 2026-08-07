package cl.zona_ti.compra_service.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class AuthDto {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AuthPublicoResponse(
            String success,
            String trace,
            AuthPublicoPayload payload,
            Object errores) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AuthPublicoPayload(
            String access_token,
            Integer expires_in,
            Integer refresh_expires_in,
            String refresh_token,
            String token_type,
            String session_state,
            String scope) {
    }
}