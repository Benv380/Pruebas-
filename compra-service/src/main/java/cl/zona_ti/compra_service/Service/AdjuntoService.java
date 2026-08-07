package cl.zona_ti.compra_service.Service;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import cl.zona_ti.compra_service.Client.AdjuntoClient;
import cl.zona_ti.compra_service.Dto.AdjuntoDto.AdjuntoListadoResponse;

@Service
public class AdjuntoService {

    private final AdjuntoClient adjuntoClient;
    private final TokenCacheService tokenCacheService;

    public AdjuntoService(AdjuntoClient adjuntoClient, TokenCacheService tokenCacheService) {
        this.adjuntoClient = adjuntoClient;
        this.tokenCacheService = tokenCacheService;
    }

    public AdjuntoListadoResponse listar(String codigoCompra) {
        return adjuntoClient.listar(codigoCompra);
    }

    public ResponseEntity<byte[]> descargar(String uuid) {
        ResponseEntity<byte[]> original = adjuntoClient.descargar(uuid);

        MediaType contentType = original.getHeaders().getContentType();
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.status(original.getStatusCode())
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(original.getBody());
    }

    // Se deja disponible por si en el futuro se necesita en el frontend,
    // aunque ya no se usa para descargar directo (bloqueado por CORS).
    public Map<String, String> obtenerCredencialesParaFrontend() {
        return Map.of(
                "access_token", tokenCacheService.getToken(),
                "user_key", adjuntoClient.getUserKey()
        );
    }
}