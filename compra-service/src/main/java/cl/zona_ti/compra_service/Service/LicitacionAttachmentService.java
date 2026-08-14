package cl.zona_ti.compra_service.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import cl.zona_ti.compra_service.Client.LicitacionAttachmentScraper;
import cl.zona_ti.compra_service.Client.LicitacionAttachmentScraper.AttachmentFile;
import cl.zona_ti.compra_service.Dto.AdjuntoLicitacionDto.AdjuntoLicitacionArchivo;
import cl.zona_ti.compra_service.Dto.AdjuntoLicitacionDto.AdjuntoLicitacionListadoPayload;
import cl.zona_ti.compra_service.Dto.AdjuntoLicitacionDto.AdjuntoLicitacionListadoResponse;
import cl.zona_ti.compra_service.Model.AdjuntoLicitacionEntity;
import cl.zona_ti.compra_service.Repository.AdjuntoLicitacionRepository;

/**
 * Capa de orquestación para adjuntos de licitaciones normales (LS/LP/LE).
 * Es el equivalente de AdjuntoService (que ya tienes para Compra Ágil), pero
 * en vez de llamar a una API REST, por debajo usa LicitacionAttachmentScraper
 * para navegar el sitio y descargar los binarios.
 *
 * Misma idea de cache que AdjuntoService: si ya descargamos los adjuntos de
 * esa licitación hace poco (dentro del TTL), no volvemos a scrapear.
 */
@Service
public class LicitacionAttachmentService {

    private final LicitacionAttachmentScraper scraper;
    private final AdjuntoLicitacionRepository repository;

    @Value("${compra-service.cache.ttl-minutos:10}")
    private long ttlMinutos;

    @Value("${compra-service.licitaciones.storage-dir:/data/adjuntos-licitaciones}")
    private String storageDir;

    public LicitacionAttachmentService(LicitacionAttachmentScraper scraper,
                                        AdjuntoLicitacionRepository repository) {
        this.scraper = scraper;
        this.repository = repository;
    }

    /**
     * Punto de entrada principal. Recibe solo el código público de la licitación
     * (ej: "2378-71-LS26") -- el mismo que ya entrega la API oficial de
     * licitaciones. Ya no se necesita el "qs": se descubrió que
     * DetailsAcquisition.aspx acepta directamente "?idlicitacion=CODIGO" y el
     * servidor resuelve el resto internamente.
     */
    public List<AdjuntoLicitacionEntity> obtenerAdjuntos(String codigoLicitacion) {
        List<AdjuntoLicitacionEntity> cacheados = repository.findByCodigoLicitacion(codigoLicitacion);
        if (!cacheados.isEmpty() && estanFrescos(cacheados)) {
            return cacheados;
        }

        try {
            String carpetaLicitacion = storageDir + "/" + sanitize(codigoLicitacion);
            List<AttachmentFile> descargados = scraper.descargarAdjuntos(codigoLicitacion, carpetaLicitacion);

            repository.deleteAll(cacheados);

            LocalDateTime ahora = LocalDateTime.now();
            List<AdjuntoLicitacionEntity> nuevos = descargados.stream().map(archivo -> {
                AdjuntoLicitacionEntity entity = new AdjuntoLicitacionEntity();
                entity.setCodigoLicitacion(codigoLicitacion);
                entity.setNombreArchivo(archivo.nombre());
                entity.setRutaArchivo(archivo.rutaLocal());
                entity.setTamanoBytes(archivo.tamanoBytes());
                entity.setFechaSync(ahora);
                return entity;
            }).toList();

            return repository.saveAll(nuevos);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("No se pudieron descargar los adjuntos de la licitación "
                    + codigoLicitacion + ": " + e.getMessage(), e);
        }
    }

    /**
     * Equivalente de AdjuntoService.listar(): trae (o refresca) los adjuntos
     * de la licitación y los devuelve en el mismo formato payload.files que
     * ya consume el frontend para Compra Ágil.
     */
    public AdjuntoLicitacionListadoResponse listar(String codigoLicitacion) {
        List<AdjuntoLicitacionEntity> adjuntos = obtenerAdjuntos(codigoLicitacion);
        List<AdjuntoLicitacionArchivo> files = adjuntos.stream()
                .map(entity -> new AdjuntoLicitacionArchivo(String.valueOf(entity.getId()), entity.getNombreArchivo()))
                .toList();
        return new AdjuntoLicitacionListadoResponse("true", new AdjuntoLicitacionListadoPayload(files));
    }

    /**
     * Equivalente de AdjuntoService.descargar(), pero acá el binario ya está
     * en disco (lo dejó obtenerAdjuntos): no hay que volver a pedirlo afuera.
     */
    public ResponseEntity<byte[]> descargar(Long id) {
        AdjuntoLicitacionEntity entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Adjunto de licitación no encontrado: " + id));

        try {
            Path path = Path.of(entity.getRutaArchivo());
            byte[] bytes = Files.readAllBytes(path);
            return ResponseEntity.ok()
                    .contentType(resolverContentType(path))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(bytes);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el adjunto " + id + ": " + e.getMessage(), e);
        }
    }

    // El archivo quedó guardado en disco sin content-type propio (viene de un
    // POST binario), así que se infiere por extensión igual que hace el
    // navegador; Files.probeContentType primero por si el SO ya lo resuelve.
    private MediaType resolverContentType(Path path) {
        try {
            String probado = Files.probeContentType(path);
            if (probado != null) {
                return MediaType.parseMediaType(probado);
            }
        } catch (IOException ignored) {
            // Se sigue con el fallback por extensión.
        }

        String nombre = path.getFileName().toString().toLowerCase();
        if (nombre.endsWith(".pdf")) return MediaType.APPLICATION_PDF;
        if (nombre.endsWith(".docx")) return MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        if (nombre.endsWith(".xlsx")) return MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        if (nombre.endsWith(".doc")) return MediaType.parseMediaType("application/msword");
        if (nombre.endsWith(".xls")) return MediaType.parseMediaType("application/vnd.ms-excel");
        if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (nombre.endsWith(".png")) return MediaType.IMAGE_PNG;
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private boolean estanFrescos(List<AdjuntoLicitacionEntity> cacheados) {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(ttlMinutos);
        return cacheados.stream().allMatch(e -> e.getFechaSync() != null && e.getFechaSync().isAfter(limite));
    }

    private String sanitize(String value) {
        return value.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}