package cl.zona_ti.compra_service.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import cl.zona_ti.compra_service.Dto.AdjuntoLicitacionDto.AdjuntoLicitacionArchivo;
import cl.zona_ti.compra_service.Dto.AdjuntoLicitacionDto.AdjuntoLicitacionListadoPayload;
import cl.zona_ti.compra_service.Dto.AdjuntoLicitacionDto.AdjuntoLicitacionListadoResponse;
import cl.zona_ti.compra_service.Model.AdjuntoLicitacionEntity;
import cl.zona_ti.compra_service.Repository.AdjuntoLicitacionRepository;

/**
 * Capa de lectura de adjuntos de licitaciones normales (LS/LP/LE).
 *
 * A diferencia de la versión anterior, esta clase YA NO dispara scraping/Selenium
 * cuando el usuario pide los adjuntos -- eso ahora lo hace exclusivamente
 * LicitacionSyncScheduler en background, cada 10 minutos. Este servicio solo
 * lee lo que ya quedó guardado en la base y en disco.
 *
 * Mismo formato de respuesta que AdjuntoService (Compra Ágil): { success, payload: { files } },
 * para que el frontend trate ambos casos igual.
 */
@Service
public class LicitacionAttachmentService {

    private final AdjuntoLicitacionRepository repository;

    public LicitacionAttachmentService(AdjuntoLicitacionRepository repository) {
        this.repository = repository;
    }

    public AdjuntoLicitacionListadoResponse listar(String codigoLicitacion) {
        List<AdjuntoLicitacionEntity> entidades = repository.findByCodigoLicitacion(codigoLicitacion);

        List<AdjuntoLicitacionArchivo> archivos = entidades.stream()
                .map(e -> new AdjuntoLicitacionArchivo(String.valueOf(e.getId()), e.getNombreArchivo()))
                .toList();

        return new AdjuntoLicitacionListadoResponse("true", new AdjuntoLicitacionListadoPayload(archivos));
    }

    public ResponseEntity<byte[]> descargar(Long id) {
        AdjuntoLicitacionEntity entidad = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el adjunto con id " + id));

        Path ruta = Path.of(entidad.getRutaArchivo());
        byte[] contenido;
        try {
            contenido = Files.readAllBytes(ruta);
        } catch (IOException e) {
            // Si el archivo desapareció del disco (ej: se borró el volumen) pero el
            // registro en la base seguía ahí, tratamos esto igual que "no encontrado" --
            // el próximo ciclo del scheduler lo va a volver a descargar de todas formas.
            throw new NoSuchElementException(
                    "El adjunto " + id + " está registrado pero no se pudo leer el archivo en disco (" + ruta + ")");
        }

        MediaType tipo;
        try {
            String probado = Files.probeContentType(ruta);
            tipo = probado != null ? MediaType.parseMediaType(probado) : MediaType.APPLICATION_OCTET_STREAM;
        } catch (IOException e) {
            tipo = MediaType.APPLICATION_OCTET_STREAM;
        }

        String nombreArchivo = entidad.getNombreArchivo() != null ? entidad.getNombreArchivo() : ruta.getFileName().toString();

        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .body(contenido);
    }
}