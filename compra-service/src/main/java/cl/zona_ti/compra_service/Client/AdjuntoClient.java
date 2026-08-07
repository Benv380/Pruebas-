package cl.zona_ti.compra_service.Client;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import cl.zona_ti.compra_service.Dto.AdjuntoDto.AdjuntoListadoResponse;
import cl.zona_ti.compra_service.Service.TokenCacheService;
import tools.jackson.databind.ObjectMapper;

@Component
public class AdjuntoClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenCacheService tokenCacheService;
    private final String userKey;

    public AdjuntoClient(
            @Value("${mercado-publico.adjuntos.url}") String baseUrl,
            @Value("${mercado-publico.adjuntos.user-key}") String userKey,
            TokenCacheService tokenCacheService) {
        this.userKey = userKey;
        this.tokenCacheService = tokenCacheService;

        // Usamos el cliente HTTP moderno de Java (java.net.http.HttpClient) en vez
        // del SimpleClientHttpRequestFactory clasico, porque este ultimo tiene un
        // bug conocido con servidores que piden renegociacion TLS a mitad de la
        // conexion (justo lo que hace adjunto.mercadopublico.cl), y se queda
        // colgado indefinidamente en vez de completar el handshake.
        HttpClient jdkHttpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(jdkHttpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(20));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                })
                .build();
    }

    public AdjuntoListadoResponse listar(String codigoCompra) {
        // Pedimos SIEMPRE texto crudo (evita conflictos con el content-type real
        // que envia Mercado Publico, que a veces no es application/json) y lo
        // parseamos nosotros mismos.
        String raw = restClient.get()
                .uri("/v1/adjuntos-compra-agil/listar/{codigo}", codigoCompra)
                .header("Authorization", "Bearer " + tokenCacheService.getToken())
                .header("user_key", userKey)
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readValue(raw, AdjuntoListadoResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Respuesta inesperada de adjuntos: " + raw, e);
        }
    }

    public ResponseEntity<byte[]> descargar(String uuid) {
        System.out.println("Descargando adjunto UUID: " + uuid);
        try {
            ResponseEntity<byte[]> resultado = restClient.get()
                    .uri("/v1/adjuntos-compra-agil/descargar/{uuid}", uuid)
                    .header("Authorization", "Bearer " + tokenCacheService.getToken())
                    .header("user_key", userKey)
                    .retrieve()
                    .toEntity(byte[].class);
            System.out.println("Descarga OK, status: " + resultado.getStatusCode()
                    + ", content-type: " + resultado.getHeaders().getContentType()
                    + ", bytes: " + (resultado.getBody() != null ? resultado.getBody().length : 0));
            return resultado;
        } catch (Exception e) {
            System.out.println("ERROR al descargar adjunto: " + e.getClass().getName() + " - " + e.getMessage());
            throw e;
        }
    }

    public String getUserKey() {
        return userKey;
    }
}