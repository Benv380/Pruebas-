package cl.zona_ti.compra_service.Model;

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

// Un documento listado dentro del Item/Detalle de Compra Agil (campo "documentos").
// Distinto de AdjuntoEntity: este viene embebido en la respuesta de listado/detalle,
// mientras que los adjuntos se piden aparte via AdjuntoClient.
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "compra_agil_documentos")
public class CompraAgilDocumentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_agil_codigo")
    private CompraAgilEntity compraAgil;

    @Column(name = "id_externo")
    private String idExterno;

    private String nombre;
}
