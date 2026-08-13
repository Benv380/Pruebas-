package cl.zona_ti.compra_service.Model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Un proveedor cotizando dentro del detalle de una Compra Agil
// (Detalle.proveedores_cotizando). La PK es id_cotizacion, la clave natural
// que ya trae la API.
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "compra_agil_proveedores_cotizando")
public class CompraAgilProveedorCotizandoEntity {

    @Id
    @Column(name = "id_cotizacion")
    private Long idCotizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_agil_codigo")
    private CompraAgilEntity compraAgil;

    @Column(name = "codigo_empresa")
    private String codigoEmpresa;

    @Column(name = "codigo_sucursal_empresa")
    private String codigoSucursalEmpresa;

    @Column(name = "es_emt")
    private Integer esEmt;

    @Column(name = "razon_social")
    private String razonSocial;

    @Column(name = "rut_proveedor")
    private String rutProveedor;

    private String descripcion;

    @Column(name = "fecha_vigencia")
    private String fechaVigencia;

    @Column(name = "fecha_creacion")
    private String fechaCreacion;

    @Column(name = "valor_neto")
    private BigDecimal valorNeto;

    @Column(name = "total_impuesto")
    private BigDecimal totalImpuesto;

    @Column(name = "monto_despacho")
    private BigDecimal montoDespacho;

    @Column(name = "monto_total")
    private BigDecimal montoTotal;

    @Column(name = "proveedor_seleccionado")
    private Integer proveedorSeleccionado;

    @Column(name = "descripcion_cotizacion")
    private String descripcionCotizacion;

    private Integer estado;

    @Column(name = "justificacion_inadmisibilidad")
    private String justificacionInadmisibilidad;

    @Column(name = "estado_por_comprador")
    private Integer estadoPorComprador;

    private Integer activo;

    @Column(name = "id_oc")
    private Long idOc;

    @Column(name = "nombre_impuesto")
    private String nombreImpuesto;

    @Column(name = "porcentaje_impuesto")
    private Integer porcentajeImpuesto;

    @OneToMany(mappedBy = "proveedorCotizando", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CompraAgilProductoCotizadoEntity> productosCotizados = new ArrayList<>();
}
