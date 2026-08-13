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

// Un item (producto/servicio solicitado) dentro del detalle de una Licitacion
// (Listado[].Items.Listado). Solo llega en el detalle por codigo, no en el
// listado por fecha (ver comentario en LicitacionService).
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "licitacion_items")
public class LicitacionItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licitacion_codigo_externo")
    private LicitacionEntity licitacion;

    private Integer correlativo;

    @Column(name = "codigo_producto")
    private Long codigoProducto;

    @Column(name = "codigo_categoria")
    private String codigoCategoria;

    private String categoria;

    @Column(name = "nombre_producto")
    private String nombreProducto;

    private String descripcion;

    @Column(name = "unidad_medida")
    private String unidadMedida;

    private BigDecimal cantidad;
}
