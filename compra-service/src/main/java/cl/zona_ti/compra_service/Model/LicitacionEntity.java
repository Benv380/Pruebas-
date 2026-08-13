package cl.zona_ti.compra_service.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    @Column(name = "dias_cierre_licitacion")
    private String diasCierreLicitacion;

    private Integer modalidad;

    @Column(name = "tipo_pago")
    private String tipoPago;

    private String tiempo;

    @Column(name = "unidad_tiempo")
    private String unidadTiempo;

    @Column(name = "tiempo_duracion_contrato")
    private String tiempoDuracionContrato;

    @Column(name = "unidad_tiempo_duracion_contrato")
    private Integer unidadTiempoDuracionContrato;

    @Column(name = "es_renovable")
    private Integer esRenovable;

    @Column(name = "fuente_financiamiento")
    private String fuenteFinanciamiento;

    @Column(name = "nombre_responsable_pago")
    private String nombreResponsablePago;

    @Column(name = "email_responsable_pago")
    private String emailResponsablePago;

    @Column(name = "nombre_responsable_contrato")
    private String nombreResponsableContrato;

    @Column(name = "email_responsable_contrato")
    private String emailResponsableContrato;

    @Column(name = "fono_responsable_contrato")
    private String fonoResponsableContrato;

    // --- Comprador ---
    @Column(name = "codigo_organismo")
    private String codigoOrganismo;

    @Column(name = "nombre_organismo")
    private String nombreOrganismo;

    @Column(name = "rut_unidad")
    private String rutUnidad;

    @Column(name = "codigo_unidad")
    private String codigoUnidad;

    @Column(name = "nombre_unidad")
    private String nombreUnidad;

    @Column(name = "direccion_unidad")
    private String direccionUnidad;

    @Column(name = "comuna_unidad")
    private String comunaUnidad;

    @Column(name = "region_unidad")
    private String regionUnidad;

    @Column(name = "rut_usuario")
    private String rutUsuario;

    @Column(name = "codigo_usuario")
    private String codigoUsuario;

    @Column(name = "nombre_usuario")
    private String nombreUsuario;

    @Column(name = "cargo_usuario")
    private String cargoUsuario;

    // --- Fechas ---
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_final")
    private LocalDateTime fechaFinal;

    @Column(name = "fecha_pub_respuestas")
    private LocalDateTime fechaPubRespuestas;

    @Column(name = "fecha_acto_apertura_tecnica")
    private LocalDateTime fechaActoAperturaTecnica;

    @Column(name = "fecha_acto_apertura_economica")
    private LocalDateTime fechaActoAperturaEconomica;

    @Column(name = "fecha_visita_terreno")
    private LocalDateTime fechaVisitaTerreno;

    @Column(name = "fecha_entrega_antecedentes")
    private LocalDateTime fechaEntregaAntecedentes;

    @Column(name = "fecha_estimada_adjudicacion")
    private LocalDateTime fechaEstimadaAdjudicacion;

    @Column(name = "fecha_adjudicacion")
    private LocalDateTime fechaAdjudicacion;

    @Column(name = "fecha_estimada_firma")
    private LocalDateTime fechaEstimadaFirma;

    @Column(name = "fecha_soporte_fisico")
    private LocalDateTime fechaSoporteFisico;

    @Column(name = "fecha_sync")
    private LocalDateTime fechaSync;

    @OneToMany(mappedBy = "licitacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LicitacionItemEntity> items = new ArrayList<>();
}
