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

// Copia local de una Compra Agil (API Compra Agil v2), tanto la version
// resumida (listado) como la completa (detalle). La PK es el "codigo" natural
// de Mercado Publico. Los hijos (productos solicitados, proveedores
// cotizando, documentos) solo llegan completos en el detalle; en el listado
// se guarda solo lo que trae el Item y se dejan como estaban si ya existian.
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "compras_agiles")
public class CompraAgilEntity {

    @Id
    private String codigo;

    private String nombre;
    private String descripcion;

    @Column(name = "id_estado")
    private Integer idEstado;

    @Column(name = "estado_codigo")
    private String estadoCodigo;

    @Column(name = "estado_glosa")
    private String estadoGlosa;

    @Column(name = "convocatoria_estado")
    private Integer convocatoriaEstado;

    @Column(name = "convocatoria_descripcion")
    private String convocatoriaDescripcion;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "fecha_ultimo_cambio")
    private LocalDateTime fechaUltimoCambio;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "fecha_cierre_primer_llamado")
    private LocalDateTime fechaCierrePrimerLlamado;

    @Column(name = "fecha_cierre_segundo_llamado")
    private LocalDateTime fechaCierreSegundoLlamado;

    @Column(name = "direccion_entrega")
    private String direccionEntrega;

    @Column(name = "plazo_entrega_dias")
    private Integer plazoEntregaDias;

    @Column(name = "tipo_presupuesto")
    private String tipoPresupuesto;

    private String moneda;

    @Column(name = "presupuesto_estimado")
    private BigDecimal presupuestoEstimado;

    @Column(name = "monto_disponible")
    private BigDecimal montoDisponible;

    @Column(name = "monto_disponible_clp")
    private BigDecimal montoDisponibleClp;

    @Column(name = "valor_cambio_moneda")
    private BigDecimal valorCambioMoneda;

    @Column(name = "fecha_cambio_moneda")
    private String fechaCambioMoneda;

    @Column(name = "id_orden_compra")
    private Long idOrdenCompra;

    @Column(name = "organismo_comprador")
    private String organismoComprador;

    @Column(name = "rut_institucion")
    private String rutInstitucion;

    @Column(name = "unidad_compra")
    private String unidadCompra;

    private Integer region;

    @Column(name = "nombre_region")
    private String nombreRegion;

    @Column(name = "multa_sancion")
    private BigDecimal multaSancion;

    @Column(name = "total_ofertas_recibidas")
    private Integer totalOfertasRecibidas;

    @Column(name = "total_demandas")
    private Integer totalDemandas;

    @Column(name = "motivo_cancelacion")
    private String motivoCancelacion;

    @Column(name = "motivo_desierta")
    private String motivoDesierta;

    @Column(name = "motivo_seleccion")
    private String motivoSeleccion;

    @Column(name = "considera_req_medioambientales")
    private Boolean consideraRequisitosMedioambientales;

    @Column(name = "considera_req_impacto_social")
    private Boolean consideraRequisitosImpactoSocial;

    // true solo cuando se guardo desde el Detalle completo (GET /{codigo}), no
    // desde el listado resumido (Item). Ver comentario en schema.sql.
    @Column(name = "detalle_completo")
    private Boolean detalleCompleto = false;

    @Column(name = "fecha_sync")
    private LocalDateTime fechaSync;

    @OneToMany(mappedBy = "compraAgil", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CompraAgilProductoSolicitadoEntity> productosSolicitados = new ArrayList<>();

    @OneToMany(mappedBy = "compraAgil", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CompraAgilProveedorCotizandoEntity> proveedoresCotizando = new ArrayList<>();

    @OneToMany(mappedBy = "compraAgil", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CompraAgilDocumentoEntity> documentos = new ArrayList<>();
}
