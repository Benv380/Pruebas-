package cl.zona_ti.compra_service.Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import cl.zona_ti.compra_service.Dto.AuthDto;
import tools.jackson.databind.ObjectMapper;

@Component
public class AuthClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthClient(@Value("${mercado-publico.auth.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                })
                .build();
    }

    public AuthDto.AuthPublicoPayload obtenerTokenPublico() {
        String raw = restClient.get()
                .uri("/v1/auth/publico")
                .retrieve()
                .body(String.class);

        try {
            AuthDto.AuthPublicoResponse respuesta = objectMapper.readValue(raw, AuthDto.AuthPublicoResponse.class);
            return respuesta.payload();
        } catch (Exception e) {
            throw new RuntimeException("Respuesta inesperada de auth/publico: " + raw, e);
        }
    }
}