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

// Un producto solicitado dentro del detalle de una Compra Agil (Detalle.productos_solicitados).
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "compra_agil_productos_solicitados")
public class CompraAgilProductoSolicitadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_agil_codigo")
    private CompraAgilEntity compraAgil;

    @Column(name = "codigo_producto")
    private String codigoProducto;

    private String nombre;
    private String descripcion;
    private BigDecimal cantidad;

    @Column(name = "unidad_medida")
    private String unidadMedida;
}
