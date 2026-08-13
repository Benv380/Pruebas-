package cl.zona_ti.compra_service.Util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

// Las fechas que devuelven las APIs de Mercado Publico (Licitacion y Compra
// Agil) no vienen todas en un unico formato: a veces traen offset/zona
// (ISO-8601 con "Z" o "+00:00") y a veces vienen como fecha y hora simple sin
// zona. Este parser se usa en los mappers para guardar todo como
// LocalDateTime (hora local, sin zona) al cachear, sin reventar el guardado
// completo si un campo puntual viene en un formato inesperado.
public final class FechaParser {

    private static final List<DateTimeFormatter> FORMATOS_SIN_ZONA = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

    private FechaParser() {
    }

    public static LocalDateTime parse(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(fecha).atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }
        for (DateTimeFormatter formato : FORMATOS_SIN_ZONA) {
            try {
                return LocalDateTime.parse(fecha, formato);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    public static String format(LocalDateTime fecha) {
        return fecha == null ? null : fecha.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
