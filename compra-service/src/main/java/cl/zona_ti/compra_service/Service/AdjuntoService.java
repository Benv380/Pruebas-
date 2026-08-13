package cl.zona_ti.compra_service.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import cl.zona_ti.compra_service.Client.AdjuntoClient;
import cl.zona_ti.compra_service.Dto.AdjuntoDto.AdjuntoArchivo;
import cl.zona_ti.compra_service.Dto.AdjuntoDto.AdjuntoListadoPayload;
import cl.zona_ti.compra_service.Dto.AdjuntoDto.AdjuntoListadoResponse;
import cl.zona_ti.compra_service.Model.AdjuntoEntity;
import cl.zona_ti.compra_service.Repository.AdjuntoRepository;

@Service
public class AdjuntoService {

    private final AdjuntoClient adjuntoClient;
    private final TokenCacheService tokenCacheService;
    private final AdjuntoRepository adjuntoRepository;

    // Mismo TTL que Licitacion/CompraAgil (ver esos Service).
    @Value("${compra-service.cache.ttl-minutos:10}")
    private long ttlMinutos;

    public AdjuntoService(AdjuntoClient adjuntoClient, TokenCacheService tokenCacheService,
            AdjuntoRepository adjuntoRepository) {
        this.adjuntoClient = adjuntoClient;
        this.tokenCacheService = tokenCacheService;
        this.adjuntoRepository = adjuntoRepository;
    }

    public AdjuntoListadoResponse listar(String codigoCompra) {
        List<AdjuntoEntity> cacheados = adjuntoRepository.findByCompraAgilCodigo(codigoCompra);
        if (!cacheados.isEmpty() && estanFrescos(cacheados)) {
            return construirRespuestaDesdeCache(cacheados);
        }

        AdjuntoListadoResponse respuesta = adjuntoClient.listar(codigoCompra);
        guardarEnCache(codigoCompra, respuesta);
        return respuesta;
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

    private AdjuntoListadoResponse construirRespuestaDesdeCache(List<AdjuntoEntity> cacheados) {
        List<AdjuntoArchivo> files = cacheados.stream()
                .map(entity -> new AdjuntoArchivo(entity.getId(), entity.getNombreArchivo()))
                .toList();
        return new AdjuntoListadoResponse("true", null, new AdjuntoListadoPayload(files), null);
    }

    private void guardarEnCache(String codigoCompra, AdjuntoListadoResponse respuesta) {
        if (respuesta == null || respuesta.payload() == null || respuesta.payload().files() == null) {
            return;
        }
        try {
            // Se reemplaza entero el set de adjuntos de ese codigo: mas simple que
            // hacer merge y el listado de adjuntos de una compra no suele achicarse.
            adjuntoRepository.deleteAll(adjuntoRepository.findByCompraAgilCodigo(codigoCompra));

            LocalDateTime ahora = LocalDateTime.now();
            List<AdjuntoEntity> nuevos = respuesta.payload().files().stream()
                    .map(archivo -> {
                        AdjuntoEntity entity = new AdjuntoEntity();
                        entity.setId(archivo.id());
                        entity.setCompraAgilCodigo(codigoCompra);
                        entity.setNombreArchivo(archivo.nombreArchivo());
                        entity.setFechaSync(ahora);
                        return entity;
                    })
                    .toList();
            adjuntoRepository.saveAll(nuevos);
        } catch (Exception ignored) {
            // Un fallo al escribir en la cache no debe tumbar la respuesta al usuario.
        }
    }

    private boolean estanFrescos(List<AdjuntoEntity> cacheados) {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(ttlMinutos);
        return cacheados.stream().allMatch(entity -> entity.getFechaSync() != null && entity.getFechaSync().isAfter(limite));
    }
}
