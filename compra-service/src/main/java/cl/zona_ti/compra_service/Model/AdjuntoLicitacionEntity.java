package cl.zona_ti.compra_service.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa, en base de datos, un adjunto ya descargado de una licitación
 * normal (LS/LP/LE). Es el equivalente de AdjuntoEntity pero para el flujo
 * de licitaciones (scraping) en vez de Compra Ágil (API REST).
 */
@Entity
@Table(name = "adjunto_licitacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdjuntoLicitacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigoLicitacion;   // ej: "2378-71-LS26"
    private String nombreArchivo;      // nombre original del archivo
    private String rutaArchivo;        // dónde quedó guardado en disco/storage
    private Integer tamanoBytes;
    private LocalDateTime fechaSync;
}