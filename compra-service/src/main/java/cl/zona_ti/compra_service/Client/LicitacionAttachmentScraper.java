package cl.zona_ti.compra_service.Client;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

/**
 * Descarga los adjuntos de una licitación normal (LS/LP/LE) de mercadopublico.cl,
 * replicando el flujo real de navegación (ficha -> ventana de adjuntos -> postback
 * de descarga por fila), en vez de intentar calcular el parámetro "enc" (que se
 * genera y queda solo en el servidor, no es derivable desde el cliente).
 *
 * Flujo:
 *  1) GET a la ficha de la licitación usando el código público
 *     (DetailsAcquisition.aspx?idlicitacion=CODIGO) con una sesión nueva ->
 *     el servidor resuelve internamente el "qs" y devuelve el HTML completo,
 *     que incluye un botón <input type="image" id="imgAdjuntos"> cuyo
 *     onclick ya trae el link armado hacia ViewAttachment.aspx?enc=... (o
 *     ViewAttachmentLC.aspx?enc=..., según el tipo de proceso) -- el enc lo
 *     generó el servidor al renderizar, no lo calculamos nosotros.
 *  2) GET a esa URL de adjuntos (misma sesión/cookies) -> se parsea la tabla
 *     (GridView de ASP.NET WebForms) para sacar, por cada fila: nombre de archivo
 *     y el postback (__doPostBack) que dispara la descarga.
 *  3) Por cada fila, POST reenviando TODOS los campos ocultos del formulario más
 *     el __EVENTTARGET/__EVENTARGUMENT de esa fila -> la respuesta es el binario.
 */
@Component
public class LicitacionAttachmentScraper {

    private static final String BASE = "https://www.mercadopublico.cl";

    // Cubre tanto ViewAttachment.aspx como ViewAttachmentLC.aspx, sea que
    // venga como href normal o dentro de un onclick="open('...')".
    private static final Pattern ATTACHMENT_LINK =
            Pattern.compile("(?:\\.\\./)*Attachment/ViewAttachment(?:LC)?\\.aspx\\?enc=[^\"'&\\s]+");

    private static final Pattern POSTBACK =
            Pattern.compile("__doPostBack\\('([^']+)'\\s*,\\s*'([^']*)'\\)");

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    public record AttachmentFile(String nombre, String rutaLocal, int tamanoBytes) {}

    public List<AttachmentFile> descargarAdjuntos(String codigoLicitacion, String outputDir)
            throws IOException, InterruptedException {

        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        // 1) Ficha de la licitación, usando directo el código público. El servidor
        //    resuelve el "qs" internamente, no necesitamos conocerlo nosotros.
        String detailsUrl = BASE + "/Procurement/Modules/RFB/DetailsAcquisition.aspx?idlicitacion=" + codigoLicitacion;
        String detailsHtml = get(client, detailsUrl, null);

        Matcher m = ATTACHMENT_LINK.matcher(detailsHtml);
        if (!m.find()) {
            throw new IllegalStateException(
                    "No se encontró el botón de adjuntos (imgAdjuntos) en la ficha (" + detailsUrl + "). " +
                    "HTML recibido: " + detailsHtml.length() + " caracteres. " +
                    "Si ese número es muy chico (unos pocos cientos), probablemente el servidor devolvió " +
                    "una redirección u otra página corta en vez de la ficha completa. " +
                    "Si el número es grande pero igual no aparece, puede que esta licitación no tenga " +
                    "adjuntos, o que el nombre del botón haya cambiado -- revisar el HTML real de la página.");
        }
        // El match puede venir con "../" adelante (ej: "../Attachment/ViewAttachment.aspx?enc=...");
        // lo limpiamos para armar la URL absoluta sin ambigüedad.
        String attachmentPath = m.group().replace("&amp;", "&").replaceFirst("^(\\.\\./)+", "");

        // "ViewAttachment.aspx" es solo una página "enrutadora" que redirige (por JS, no HTTP)
        // hacia "ViewAttachmentLC.aspx" con el mismo enc -- vamos directo a esa para no depender
        // de que se ejecute ningún JavaScript.
        if (!attachmentPath.contains("ViewAttachmentLC")) {
            attachmentPath = attachmentPath.replace("ViewAttachment.aspx", "ViewAttachmentLC.aspx");
        }

        String attachmentUrl = BASE + "/Procurement/Modules/" + attachmentPath;

        // 2) Página de adjuntos -> parsear filas y campos ocultos del formulario
        String listHtml = get(client, attachmentUrl, detailsUrl);
        Document doc = Jsoup.parse(listHtml, attachmentUrl);

        Map<String, String> hiddenFields = new LinkedHashMap<>();
        for (Element input : doc.select("input[type=hidden]")) {
            String name = input.attr("name");
            if (!name.isBlank()) {
                hiddenFields.put(name, input.attr("value"));
            }
        }

        // OJO: el selector de la tabla es una suposición basada en el nombre "grdId"
        // que vimos en el __VIEWSTATE capturado. Si no encuentra filas, se guarda el
        // HTML real recibido en outputDir/debug_adjuntos.html para poder inspeccionarlo.
        Elements rows = doc.select("table[id*=grdId] tr, table[id*=DWNL] tr");

        if (rows.isEmpty()) {
            Files.createDirectories(Path.of(outputDir));
            Path debugFile = Path.of(outputDir, "debug_adjuntos.html");
            Files.writeString(debugFile, listHtml);
            throw new IllegalStateException(
                    "No se encontraron filas de adjuntos en " + attachmentUrl + ". " +
                    "Guardé el HTML real recibido en: " + debugFile.toAbsolutePath() +
                    " -- ábrelo para ver la estructura real de la tabla y ajustar el selector.");
        }

        List<AttachmentFile> resultados = new ArrayList<>();
        Files.createDirectories(Path.of(outputDir));

        int index = 0;
        for (Element row : rows) {
            Element link = row.selectFirst("a[href*=__doPostBack]");
            if (link == null) continue;

            Matcher pm = POSTBACK.matcher(link.attr("href"));
            if (!pm.find()) continue;

            String eventTarget = pm.group(1);
            String eventArgument = pm.group(2);

            Elements cells = row.select("td");
            String fileName = !cells.isEmpty() ? cells.get(0).text().trim() : "";
            if (fileName.isBlank()) fileName = "adjunto_" + index;

            byte[] bytes = descargarBinario(client, attachmentUrl, hiddenFields, eventTarget, eventArgument);

            Path out = Path.of(outputDir, sanitize(fileName));
            Files.write(out, bytes);

            resultados.add(new AttachmentFile(fileName, out.toString(), bytes.length));
            index++;
        }

        if (resultados.isEmpty()) {
            throw new IllegalStateException(
                    "No se encontraron filas de adjuntos en " + attachmentUrl + ". " +
                    "Revisa el selector de tabla contra el HTML real de esa página.");
        }

        return resultados;
    }

    private String get(HttpClient client, String url, String referer) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .GET();
        if (referer != null) {
            builder.header("Referer", referer);
        }
        HttpResponse<String> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            throw new IOException("GET " + url + " -> HTTP " + resp.statusCode());
        }
        return resp.body();
    }

    private byte[] descargarBinario(HttpClient client, String url, Map<String, String> hiddenFields,
                                     String eventTarget, String eventArgument)
            throws IOException, InterruptedException {

        Map<String, String> fields = new LinkedHashMap<>(hiddenFields);
        fields.put("__EVENTTARGET", eventTarget);
        fields.put("__EVENTARGUMENT", eventArgument);

        StringBuilder form = new StringBuilder();
        for (Map.Entry<String, String> e : fields.entrySet()) {
            if (form.length() > 0) form.append("&");
            form.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
        }

        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Referer", url)
                .header("Origin", BASE)
                .POST(BodyPublishers.ofString(form.toString()))
                .build();

        HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() >= 400) {
            throw new IOException("POST " + url + " -> HTTP " + resp.statusCode());
        }
        return resp.body();
    }

    private String sanitize(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}