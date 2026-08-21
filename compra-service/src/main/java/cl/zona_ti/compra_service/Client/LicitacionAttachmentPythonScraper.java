package cl.zona_ti.compra_service.Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Descarga los adjuntos de una licitación normal (LS/LP/LE) de
 * mercadopublico.cl invocando como subproceso el script Python
 * (scripts/descargar_adjuntos.py, Playwright) que reemplaza al viejo
 * scraper de Selenium.
 *
 * El script deja los archivos en un directorio temporal; este componente
 * los lee a memoria y BORRA ese directorio antes de devolver el resultado
 * -- el disco es solo un paso intermedio, la base de datos es la única
 * fuente de verdad (ver AdjuntoLicitacionEntity).
 *
 * Pensado para ser invocado SOLO desde el scheduler en background (ver
 * LicitacionSyncScheduler), nunca directamente desde un request del
 * usuario: el script abre una ventana real de Chromium (no puede ser
 * headless, el sitio lo bloquea), así que es lento y no escala a demanda.
 *
 * OJO con "headless=False": en una VM Linux sin escritorio, Chromium no
 * tiene dónde dibujarse. "command-prefix" (compra-service.python) es justo
 * para eso -- en un entorno con GUI (ej. Windows con sesión abierta) basta
 * con "python", pero en un servidor Linux headless hay que anteponerle un
 * display virtual, ej: "xvfb-run,-a,python3" (ver application.yml).
 */
@Component
public class LicitacionAttachmentPythonScraper {

    private static final Logger log = LoggerFactory.getLogger(LicitacionAttachmentPythonScraper.class);

    // Lista separada por comas: cada elemento es un argv del proceso a
    // lanzar, en orden, ANTES del script. Ej: "python" (dev con GUI) o
    // "xvfb-run,-a,--server-args=-screen 0 1920x1080x24,python3" (Linux
    // headless). Un elemento puede contener espacios (ej. --server-args=...)
    // sin problema: se pasa como un único argv, no se re-parsea por shell.
    @Value("#{'${compra-service.python.command-prefix:python}'.split(',')}")
    private List<String> commandPrefix;

    @Value("${compra-service.python.script-path:scripts/descargar_adjuntos.py}")
    private String scriptPath;

    @Value("${compra-service.python.timeout-seconds:90}")
    private long timeoutSeconds;

    public record AttachmentFile(String nombre, byte[] contenido, int tamanoBytes, String tipoContenido) {}

    public List<AttachmentFile> descargarAdjuntos(String codigoLicitacion) throws IOException {
        Path tempDir = Files.createTempDirectory("adjunto_licitacion_" + sanitize(codigoLicitacion) + "_");
        try {
            ejecutarScript(codigoLicitacion, tempDir);
            return leerArchivosDescargados(tempDir);
        } finally {
            borrarDirectorio(tempDir);
        }
    }

    private void ejecutarScript(String codigoLicitacion, Path tempDir) throws IOException {
        List<String> comando = new ArrayList<>(commandPrefix);
        comando.addAll(Arrays.asList(scriptPath, codigoLicitacion, "--out", tempDir.toString()));

        ProcessBuilder pb = new ProcessBuilder(comando);
        pb.redirectErrorStream(true);

        Process proceso = pb.start();
        String salida;
        try (var in = proceso.getInputStream()) {
            salida = new String(in.readAllBytes());
        }

        boolean terminoATiempo;
        try {
            terminoATiempo = proceso.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            proceso.destroyForcibly();
            throw new IOException("Interrumpido esperando el script de adjuntos para " + codigoLicitacion, e);
        }

        if (!terminoATiempo) {
            proceso.destroyForcibly();
            throw new IOException("El script de adjuntos para " + codigoLicitacion
                    + " no terminó dentro de " + timeoutSeconds + "s. Salida hasta el momento:\n" + salida);
        }

        if (proceso.exitValue() != 0) {
            throw new IOException("El script de adjuntos para " + codigoLicitacion
                    + " terminó con código " + proceso.exitValue() + ". Salida:\n" + salida);
        }

        log.debug("Script de adjuntos OK para {}. Salida:\n{}", codigoLicitacion, salida);
    }

    private List<AttachmentFile> leerArchivosDescargados(Path tempDir) throws IOException {
        List<AttachmentFile> resultados = new ArrayList<>();
        try (Stream<Path> archivos = Files.list(tempDir)) {
            for (Path archivo : archivos.filter(Files::isRegularFile).toList()) {
                byte[] bytes = Files.readAllBytes(archivo);
                resultados.add(new AttachmentFile(
                        archivo.getFileName().toString(),
                        bytes,
                        bytes.length,
                        adivinarTipoContenido(archivo.getFileName().toString())));
            }
        }

        if (resultados.isEmpty()) {
            throw new IOException("El script de adjuntos no dejó ningún archivo en " + tempDir);
        }
        return resultados;
    }

    private String adivinarTipoContenido(String nombreArchivo) {
        String ext = nombreArchivo.contains(".")
                ? nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "zip" -> "application/zip";
            case "rar" -> "application/vnd.rar";
            case "rtf" -> "application/rtf";
            case "kmz" -> "application/vnd.google-earth.kmz";
            case "dwg" -> "image/vnd.dwg";
            default -> "application/octet-stream";
        };
    }

    private void borrarDirectorio(Path dir) {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    log.warn("No se pudo borrar {} del directorio temporal de adjuntos.", p, e);
                }
            });
        } catch (IOException e) {
            log.warn("No se pudo limpiar el directorio temporal de adjuntos {}.", dir, e);
        }
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
