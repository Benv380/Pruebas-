package cl.zona_ti.compra_service.Scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import cl.zona_ti.compra_service.Client.LicitacionAttachmentPythonScraper;
import cl.zona_ti.compra_service.Client.LicitacionAttachmentPythonScraper.AttachmentFile;
import cl.zona_ti.compra_service.Dto.LicitacionDto.Licitacion;
import cl.zona_ti.compra_service.Dto.LicitacionDto.LicitacionResponse;
import cl.zona_ti.compra_service.Model.AdjuntoLicitacionEntity;
import cl.zona_ti.compra_service.Repository.AdjuntoLicitacionRepository;
import cl.zona_ti.compra_service.Service.LicitacionService;
import jakarta.annotation.PreDestroy;

/**
 * Cada X minutos (compra-service.sync.fixed-delay):
 *  1) Pide a LicitacionService las licitaciones recientes -- mismo método
 *     que ya usa el frontend (GET /compra/licitacion/listar), que a su vez
 *     consulta la API pública de Mercado Público y guarda cada licitación
 *     en la tabla "licitaciones" (cache). Acá NO se duplica esa llamada a
 *     mano: se reutiliza tal cual.
 *  2) Para cada código, revisa si ya hay adjuntos guardados en
 *     adjunto_licitacion. Los documentos de una licitación no cambian una
 *     vez publicados, así que "ya existe" es suficiente -- no hay TTL acá.
 *  3) Las licitaciones sin adjuntos se encolan en un pool fijo de 2
 *     descargas Python (Playwright) en paralelo -- así nunca se abren más
 *     de 2 ventanas de Chromium al mismo tiempo, sin importar cuántas
 *     licitaciones nuevas aparezcan en un ciclo.
 *
 * Si no hay nada pendiente, el ciclo no toca Python para nada -- el costo
 * caro (levantar Chromium) solo se paga cuando realmente hace falta.
 */
@Component
public class LicitacionSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(LicitacionSyncScheduler.class);

    private static final int POOL_SIZE = 2;

    private final LicitacionAttachmentPythonScraper scraper;
    private final AdjuntoLicitacionRepository repository;
    private final LicitacionService licitacionService;
    private final ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);

    public LicitacionSyncScheduler(LicitacionAttachmentPythonScraper scraper,
                                    AdjuntoLicitacionRepository repository,
                                    LicitacionService licitacionService) {
        this.scraper = scraper;
        this.repository = repository;
        this.licitacionService = licitacionService;
    }

    @Scheduled(fixedDelayString = "${compra-service.sync.fixed-delay:PT10M}")
    public void sincronizarAdjuntos() {
        List<String> codigosDelPeriodo = obtenerCodigosLicitacionesRecientes();
        if (codigosDelPeriodo.isEmpty()) {
            log.debug("Sync adjuntos: no hay licitaciones recientes en este ciclo.");
            return;
        }

        List<String> pendientes = codigosDelPeriodo.stream()
                .filter(this::necesitaSincronizar)
                .toList();

        if (pendientes.isEmpty()) {
            log.debug("Sync adjuntos: {} licitaciones revisadas, ninguna pendiente.", codigosDelPeriodo.size());
            return;
        }

        log.info("Sync adjuntos: {} licitaciones pendientes de descargar (de {} revisadas).",
                pendientes.size(), codigosDelPeriodo.size());

        for (String codigo : pendientes) {
            pool.submit(() -> descargarYGuardar(codigo));
        }
    }

    // "Ya existe" = ya tiene al menos un adjunto guardado. Sin TTL: los
    // documentos de una licitación publicada no cambian.
    private boolean necesitaSincronizar(String codigo) {
        return repository.findByCodigoLicitacion(codigo).isEmpty();
    }

    private void descargarYGuardar(String codigo) {
        try {
            List<AttachmentFile> archivos = scraper.descargarAdjuntos(codigo);

            LocalDateTime ahora = LocalDateTime.now();
            List<AdjuntoLicitacionEntity> nuevos = archivos.stream().map(a -> {
                AdjuntoLicitacionEntity e = new AdjuntoLicitacionEntity();
                e.setCodigoLicitacion(codigo);
                e.setNombreArchivo(a.nombre());
                e.setContenido(a.contenido());
                e.setTipoContenido(a.tipoContenido());
                e.setTamanoBytes(a.tamanoBytes());
                e.setFechaSync(ahora);
                return e;
            }).toList();

            repository.saveAll(nuevos);
            log.info("Sync adjuntos OK para {}: {} archivo(s).", codigo, nuevos.size());

        } catch (Exception e) {
            // No relanzar: un fallo en una licitación no debe tumbar el pool ni
            // afectar a las demás que se están procesando en paralelo. Reintentará
            // solo en el próximo ciclo (sigue "pendiente" porque no quedó guardada).
            log.warn("Sync adjuntos FALLÓ para {}: {}", codigo, e.getMessage());
        }
    }

    private List<String> obtenerCodigosLicitacionesRecientes() {
        try {
            LicitacionResponse respuesta = licitacionService.listarUltimasOchoHoras();
            if (respuesta == null || respuesta.listado() == null) {
                return List.of();
            }
            return respuesta.listado().stream()
                    .map(Licitacion::codigoExterno)
                    .filter(codigo -> codigo != null && !codigo.isBlank())
                    .toList();
        } catch (Exception e) {
            log.warn("No se pudo obtener el listado de licitaciones recientes: {}", e.getMessage());
            return List.of();
        }
    }

    @PreDestroy
    public void shutdown() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
