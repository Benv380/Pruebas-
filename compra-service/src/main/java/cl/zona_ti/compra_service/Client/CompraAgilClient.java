package cl.zona_ti.compra_service.Client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import cl.zona_ti.compra_service.Dto.CompraAgilDto.CompraAgilDetalleResponse;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.CompraAgilListadoResponse;

@Component
public class CompraAgilClient {

    private final RestClient restClient;

    public CompraAgilClient(
            @Value("${mercado-publico.compra-agil.url}") String baseUrl,
            @Value("${mercado-publico.compra-agil.ticket}") String ticket) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("ticket", ticket)
                // La API responde el envelope {success, trace, payload, errors} incluso
                // en 4xx/429 (ver seccion 7 de la doc), asi que no debe lanzar excepcion
                // por status.
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                })
                .build();
    }

    // filtros admitidos (ver doc API Compra Agil v2, seccion 5.1): ttl_cambio_ms,
    // cambio_desde, cambio_hasta, publicado_desde, publicado_hasta, estado, region,
    // id, q, tamano_pagina, numero_pagina, ordenar_por.
    public CompraAgilListadoResponse listar(Map<String, String> filtros) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/v2/compra-agil");
                    filtros.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .body(CompraAgilListadoResponse.class);
    }

    public CompraAgilDetalleResponse getDetalleByCodigo(String codigo) {
        return restClient.get()
                .uri("/v2/compra-agil/{codigo}", codigo)
                .retrieve()
                .body(CompraAgilDetalleResponse.class);
    }

}
