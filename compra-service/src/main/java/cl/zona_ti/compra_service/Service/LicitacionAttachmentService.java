package cl.zona_ti.compra_service.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import cl.zona_ti.compra_service.Model.AdjuntoLicitacionEntity;
import cl.zona_ti.compra_service.Repository.AdjuntoLicitacionRepository;

/**
 * Capa de lectura de adjuntos de licitaciones normales (LS/LP/LE).
 *
 * A diferencia de la versión anterior, esta clase YA NO dispara scraping/Selenium
 * cuando el usuario pide los adjuntos -- eso ahora lo hace exclusivamente
 * LicitacionSyncScheduler en background, cada 10 minutos. Este servicio solo
 * lee lo que ya quedó guardado en la base.
 *
 * Si el usuario pide los adjuntos de una licitación muy reciente que el
 * scheduler todavía no alcanzó a procesar, simplemente devuelve una lista
 * vacía (el frontend puede mostrar "aún sincronizando" en ese caso).
 */
@Service
public class LicitacionAttachmentService {

    private final AdjuntoLicitacionRepository repository;

    public LicitacionAttachmentService(AdjuntoLicitacionRepository repository) {
        this.repository = repository;
    }

    public List<AdjuntoLicitacionEntity> obtenerAdjuntos(String codigoLicitacion) {
        return repository.findByCodigoLicitacion(codigoLicitacion);
    }
}