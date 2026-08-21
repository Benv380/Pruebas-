package cl.zona_ti.compra_service.Service;

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
 * Esta clase NO dispara scraping cuando el usuario pide los adjuntos -- eso
 * lo hace exclusivamente LicitacionSyncScheduler en background. Este
 * servicio solo lee lo que ya quedó guardado en la base (el binario
 * completo vive en la columna "contenido", no hay archivos en disco).
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
        List<AdjuntoLicitacionArchivo> archivos = repository.listarPorCodigo(codigoLicitacion);
        return new AdjuntoLicitacionListadoResponse("true", new AdjuntoLicitacionListadoPayload(archivos));
    }

    public ResponseEntity<byte[]> descargar(Long id) {
        AdjuntoLicitacionEntity entidad = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el adjunto con id " + id));

        byte[] contenido = entidad.getContenido();
        if (contenido == null) {
            throw new NoSuchElementException("El adjunto " + id + " está registrado pero no tiene contenido guardado.");
        }

        MediaType tipo;
        try {
            tipo = entidad.getTipoContenido() != null
                    ? MediaType.parseMediaType(entidad.getTipoContenido())
                    : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception e) {
            tipo = MediaType.APPLICATION_OCTET_STREAM;
        }

        String nombreArchivo = entidad.getNombreArchivo() != null ? entidad.getNombreArchivo() : "adjunto_" + id;

        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .body(contenido);
    }
}
