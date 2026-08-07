package cl.zona_ti.compra_service.Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import cl.zona_ti.compra_service.Dto.LicitacionDto.LicitacionResponse;

@Component
public class LicitacionClient {

    private final RestClient restClient;
    private final String ticket;

    public LicitacionClient(
            @Value("${mercado-publico.licitacion.url}") String baseUrl,
            @Value("${mercado-publico.licitacion.ticket}") String ticket) {
        this.ticket = ticket;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public LicitacionResponse getLicitacionByCodigo(String codigo) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/licitaciones.json")
                        .queryParam("codigo", codigo)
                        .queryParam("ticket", ticket)
                        .build())
                .retrieve()
                .body(LicitacionResponse.class);
    }

    // fecha en formato DDMMAAAA, ej: 04082026
    public LicitacionResponse getLicitacionesPorFecha(String fecha) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/licitaciones.json")
                        .queryParam("fecha", fecha)
                        .queryParam("ticket", ticket)
                        .build())
                .retrieve()
                .body(LicitacionResponse.class);
    }
}
