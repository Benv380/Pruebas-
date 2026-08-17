package cl.zona_ti.compra_service.Scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import cl.zona_ti.compra_service.Client.LicitacionAttachmentSeleniumScraper;
import cl.zona_ti.compra_service.Client.LicitacionAttachmentSeleniumScraper.AttachmentFile;
import cl.zona_ti.compra_service.Model.AdjuntoLicitacionEntity;
import cl.zona_ti.compra_service.Repository.AdjuntoLicitacionRepository;
import jakarta.annotation.PreDestroy;

/**
 * Cada 10 minutos:
 *  1) Pregunta a la API pública de mercadopublico.cl qué licitaciones hay
 *     (mismo endpoint que ya usa LicitacionController).
 *  2) Compara contra lo que ya tenemos sincronizado en adjunto_licitacion.
 *  3) Si hay licitaciones nuevas (o vencidas según el TTL), las encola en un
 *     pool fijo de 2 Chromes headless en paralelo -- así nunca se abren más
 *     de 2 instancias de Selenium al mismo tiempo, sin importar cuántas
 *     licitaciones nuevas aparezcan en un ciclo.
 *
 * Si no hay nada nuevo, el ciclo no toca Selenium para nada -- el costo caro
 * (Chrome headless) solo se paga cuando realmente hace falta.
 */
@Component
public class LicitacionSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(LicitacionSyncScheduler.class);

    private static final int POOL_SIZE = 2;

    private final LicitacionAttachmentSeleniumScraper scraper;
    private final AdjuntoLicitacionRepository repository;
    private final RestTemplate restTemplate;
    private final ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);

    @Value("${compra-service.cache.ttl-minutos:10}")
    private long ttlMinutos;

    @Value("${compra-service.licitaciones.storage-dir:/data/adjuntos-licitaciones}")
    private String storageDir;

    // Reutiliza exactamente la misma URL base y ticket que ya usás para
    // Licitaciones en Compra Ágil/LicitacionController (application.yml:
    // mercado-publico.licitacion.*) -- no hay un ticket nuevo que gestionar.
    @Value("${mercado-publico.licitacion.url}")
    private String apiUrlBase;

    @Value("${mercado-publico.licitacion.ticket}")
    private String apiTicket;

    public LicitacionSyncScheduler(LicitacionAttachmentSeleniumScraper scraper,
                                    AdjuntoLicitacionRepository repository,
                                    RestTemplate restTemplate) {
        this.scraper = scraper;
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedDelayString = "PT10M")
    public void sincronizarAdjuntos() {
        List<String> codigosDelPeriodo = obtenerCodigosLicitacionesRecientes();
        if (codigosDelPeriodo.isEmpty()) {
            log.debug("Sync adjuntos: la API pública no devolvió licitaciones en este ciclo.");
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

    private boolean necesitaSincronizar(String codigo) {
        List<AdjuntoLicitacionEntity> cacheados = repository.findByCodigoLicitacion(codigo);
        if (cacheados.isEmpty()) return true;

        LocalDateTime limite = LocalDateTime.now().minusMinutes(ttlMinutos);
        return cacheados.stream().anyMatch(e -> e.getFechaSync() == null || e.getFechaSync().isBefore(limite));
    }

    private void descargarYGuardar(String codigo) {
        try {
            String carpeta = storageDir + "/" + sanitize(codigo);
            List<AttachmentFile> archivos = scraper.descargarAdjuntos(codigo, carpeta);

            repository.deleteAll(repository.findByCodigoLicitacion(codigo));

            LocalDateTime ahora = LocalDateTime.now();
            List<AdjuntoLicitacionEntity> nuevos = archivos.stream().map(a -> {
                AdjuntoLicitacionEntity e = new AdjuntoLicitacionEntity();
                e.setCodigoLicitacion(codigo);
                e.setNombreArchivo(a.nombre());
                e.setRutaArchivo(a.rutaLocal());
                e.setTamanoBytes((int) a.tamanoBytes());
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
            // OJO: armado genérico (base + /licitaciones.json?ticket=...&fecha=DDMMYYYY).
            // Si tu LicitacionController arma esta URL distinto (otro endpoint, otro
            // parámetro tipo "estado" o "codigo"), hay que calcarlo acá para no
            // duplicar dos formas distintas de llamar a la misma API.
            String url = apiUrlBase + "/licitaciones.json?ticket=" + apiTicket + "&fecha=" + fechaHoyDDMMYYYY();
            Map<String, Object> respuesta = restTemplate.getForObject(url, Map.class);
            if (respuesta == null || !(respuesta.get("Listado") instanceof List<?> listado)) {
                return List.of();
            }
            return listado.stream()
                    .filter(item -> item instanceof Map)
                    .map(item -> (Map<?, ?>) item)
                    .map(item -> item.get("CodigoExterno"))
                    .filter(codigo -> codigo != null)
                    .map(Object::toString)
                    .toList();
        } catch (Exception e) {
            log.warn("No se pudo consultar la API pública de licitaciones: {}", e.getMessage());
            return List.of();
        }
    }

    private String fechaHoyDDMMYYYY() {
        var hoy = java.time.LocalDate.now();
        return "%02d%02d%04d".formatted(hoy.getDayOfMonth(), hoy.getMonthValue(), hoy.getYear());
    }

    private String sanitize(String value) {
        return value.replaceAll("[\\\\/:*?\"<>|]", "_");
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