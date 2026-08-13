package cl.zona_ti.compra_service.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Copia local de una Licitacion de Mercado Publico (licitaciones.json), usada
// como cache para no tener que volver a pedir el detalle a la API externa en
// cada listado. La PK es el codigo externo, no un id autoincremental: es el
// identificador natural que ya usa Mercado Publico y con el que se consulta.
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "licitaciones")
public class LicitacionEntity {

    @Id
    @Column(name = "codigo_externo")
    private String codigoExterno;

    private String nombre;

    @Column(name = "codigo_estado")
    private Integer codigoEstado;

    private String estado;
    private String descripcion;
    private String moneda;

    @Column(name = "monto_estimado")
    private BigDecimal montoEstimado;

    private String tipo;

    @Column(name = "codigo_organismo")
    private String codigoOrganismo;

    @Column(name = "nombre_organismo")
    private String nombreOrganismo;

    @Column(name = "nombre_unidad")
    private String nombreUnidad;

    @Column(name = "region_unidad")
    private String regionUnidad;

    @Column(name = "comuna_unidad")
    private String comunaUnidad;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "fecha_adjudicacion")
    private LocalDateTime fechaAdjudicacion;

    @Column(name = "fecha_sync")
    private LocalDateTime fechaSync;
}
