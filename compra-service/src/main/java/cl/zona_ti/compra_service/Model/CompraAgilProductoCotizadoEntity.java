package cl.zona_ti.compra_service.Model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Un producto cotizado por un proveedor (ProveedorCotizando.productos_cotizados).
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "compra_agil_productos_cotizados")
public class CompraAgilProductoCotizadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cotizacion")
    private CompraAgilProveedorCotizandoEntity proveedorCotizando;

    @Column(name = "codigo_producto")
    private String codigoProducto;

    @Column(name = "nombre_producto")
    private String nombreProducto;

    private String descripcion;
    private BigDecimal cantidad;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    @Column(name = "monto_total_producto")
    private BigDecimal montoTotalProducto;
}
