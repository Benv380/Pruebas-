package cl.zona_ti.compra_service.Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

/**
 * Descarga los adjuntos de una licitación normal (LS/LP/LE) de mercadopublico.cl
 * usando un Chrome real (headless) en vez de armar las peticiones HTTP a mano.
 *
 * El sitio bloquea clientes HTTP "artesanales" (visto con LicitacionAttachmentScraper:
 * "Acceso denegado" / robot.png en Attachment/ViewAttachmentLC.aspx incluso imitando
 * headers de Chrome). Con Selenium el navegador es real: ejecuta el JS de la página,
 * maneja cookies/sesión igual que un usuario, y hace el click real sobre el botón de
 * adjuntos -- por lo tanto no dispara esa protección.
 *
 * Pensado para ser invocado SOLO desde el scheduler en background (ver
 * LicitacionSyncScheduler), nunca directamente desde un request del usuario --
 * abrir un Chrome headless por request sería lento y no escalaría.
 */
@Component
public class LicitacionAttachmentSeleniumScraper {

    private static final String BASE = "https://www.mercadopublico.cl";
    private static final Duration TIMEOUT = Duration.ofSeconds(25);

    public record AttachmentFile(String nombre, String rutaLocal, long tamanoBytes) {}

    public List<AttachmentFile> descargarAdjuntos(String codigoLicitacion, String outputDir)
            throws IOException {

        Path downloadDir = Path.of(outputDir);
        Files.createDirectories(downloadDir);

        WebDriver driver = crearDriver(downloadDir);
        try {
            String detailsUrl = BASE + "/Procurement/Modules/RFB/DetailsAcquisition.aspx?idlicitacion=" + codigoLicitacion;
            driver.get(detailsUrl);

            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

            // El botón real es <input type="image" name="imgAdjuntos" ...> y su
            // onclick abre una ventana nueva (window.open) hacia el listado de adjuntos.
            WebElement botonAdjuntos;
            try {
                botonAdjuntos = wait.until(ExpectedConditions.elementToBeClickable(
                        By.name("imgAdjuntos")));
            } catch (Exception e) {
                throw new IllegalStateException(
                        "No se encontró el botón de adjuntos (imgAdjuntos) en la ficha de "
                        + codigoLicitacion + ". Puede que esta licitación no tenga adjuntos, "
                        + "o que la estructura de la página haya cambiado.", e);
            }

            Set<String> ventanasAntes = driver.getWindowHandles();
            botonAdjuntos.click();

            // Esperar a que se abra la ventana emergente de adjuntos.
            wait.until(d -> d.getWindowHandles().size() > ventanasAntes.size());
            Set<String> ventanasDespues = driver.getWindowHandles();
            ventanasDespues.removeAll(ventanasAntes);
            String ventanaAdjuntos = ventanasDespues.iterator().next();

            driver.switchTo().window(ventanaAdjuntos);

            // Esperar a que la tabla de adjuntos cargue. Igual que en el scraper HTTP,
            // el id exacto de la tabla (GridView de ASP.NET) puede variar -- si esto
            // falla, volcamos el HTML real recibido a disco para poder ajustar el
            // selector sin tener que adivinar (antes esto siempre estaba bloqueado
            // por el anti-bot, nunca pudimos confirmarlo contra HTML real).
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("table[id*=grdId], table[id*=DWNL], table[id*=GridView]")));
            } catch (Exception e) {
                Path debugFile = downloadDir.resolve("debug_ventana_adjuntos_" + codigoLicitacion + ".html");
                try {
                    Files.writeString(debugFile, driver.getPageSource());
                } catch (IOException ioEx) {
                    // si ni siquiera se pudo escribir el debug, seguimos con el error original
                }
                throw new IllegalStateException(
                        "No se encontró la tabla de adjuntos para " + codigoLicitacion
                        + ". HTML real volcado en: " + debugFile.toAbsolutePath(), e);
            }

            List<WebElement> filas = driver.findElements(
                    By.cssSelector("table[id*=grdId] tr, table[id*=DWNL] tr, table[id*=GridView] tr"));

            List<AttachmentFile> resultados = new ArrayList<>();
            int index = 0;
            for (WebElement fila : filas) {
                List<WebElement> linksDescarga = fila.findElements(
                        By.cssSelector("a[href*=\"__doPostBack\"], a[href*=\"Download\"], input[type=image]"));
                if (linksDescarga.isEmpty()) continue;

                String nombreEsperado = "adjunto_" + index;
                try {
                    List<WebElement> celdas = fila.findElements(By.tagName("td"));
                    if (!celdas.isEmpty() && !celdas.get(0).getText().isBlank()) {
                        nombreEsperado = celdas.get(0).getText().trim();
                    }
                } catch (Exception ignored) { }

                long archivosAntes = contarArchivos(downloadDir);
                linksDescarga.get(0).click();

                Path archivoDescargado = esperarDescarga(downloadDir, archivosAntes, TIMEOUT);
                if (archivoDescargado != null) {
                    resultados.add(new AttachmentFile(
                            nombreEsperado,
                            archivoDescargado.toString(),
                            Files.size(archivoDescargado)));
                }
                index++;
            }

            if (resultados.isEmpty()) {
                throw new IllegalStateException(
                        "Se encontró la ventana de adjuntos de " + codigoLicitacion
                        + " pero no se pudo descargar ningún archivo. Revisar selectores/flujo con debug.");
            }

            return resultados;

        } finally {
            driver.quit();
        }
    }

    private WebDriver crearDriver(Path downloadDir) {
        // En el contenedor (Alpine) Chromium y chromedriver vienen instalados por
        // apk, no descargados por Selenium Manager -- hay que apuntarle las rutas
        // explícitamente vía las variables de entorno seteadas en el Dockerfile.
        String chromeBin = System.getenv("CHROME_BIN");
        String chromeDriverBin = System.getenv("CHROMEDRIVER_BIN");
        if (chromeDriverBin != null && !chromeDriverBin.isBlank()) {
            System.setProperty("webdriver.chrome.driver", chromeDriverBin);
        }

        ChromeOptions options = new ChromeOptions();
        if (chromeBin != null && !chromeBin.isBlank()) {
            options.setBinary(chromeBin);
        }
        options.addArguments(
                // SIN --headless: corre con interfaz gráfica real sobre el display
                // virtual (Xvfb) que levanta entrypoint.sh -- el modo headless dejaba
                // huellas detectables (fingerprint de renderizado, plugins ausentes,
                // etc.) que el anti-bot del sitio bloqueaba pese a enmascarar
                // navigator.webdriver.
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--window-size=1920,1080",
                "--disable-blink-features=AutomationControlled",
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
        );
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadDir.toAbsolutePath().toString());
        prefs.put("download.prompt_for_download", false);
        prefs.put("plugins.always_open_pdf_externally", true);
        options.setExperimentalOption("prefs", prefs);

        ChromeDriver driver = new ChromeDriver(options);

        // Refuerzo extra: sobreescribe navigator.webdriver ANTES de que cargue
        // cualquier página (se re-ejecuta en cada navegación/documento nuevo),
        // para que el JS del sitio no pueda leerlo como "true".
        driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", Map.of(
                "source", "Object.defineProperty(navigator, 'webdriver', { get: () => undefined });"
        ));

        return driver;
    }

    private long contarArchivos(Path dir) throws IOException {
        try (Stream<Path> s = Files.list(dir)) {
            return s.count();
        }
    }

    /**
     * Chrome descarga a un archivo temporal ".crdownload" mientras está en progreso.
     * Esperamos a que aparezca un archivo nuevo y termine de descargarse (deja de
     * tener extensión .crdownload).
     */
    private Path esperarDescarga(Path dir, long cantidadAntes, Duration timeout) throws IOException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            try (Stream<Path> s = Files.list(dir)) {
                List<Path> archivos = s.filter(p -> !p.toString().endsWith(".crdownload")).toList();
                if (archivos.size() > cantidadAntes) {
                    return archivos.get(archivos.size() - 1);
                }
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
    }
}