package cl.zona_ti.compra_service.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Metadato de un adjunto de Compra Agil (AdjuntoDto.AdjuntoArchivo). El
// contenido binario no se guarda aca: AdjuntoService.descargar sigue haciendo
// streaming directo desde adjunto.mercadopublico.cl, esto solo cachea el
// listado (id + nombre) por codigo de compra.
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "adjuntos")
public class AdjuntoEntity {

    @Id
    private String id;

    @Column(name = "compra_agil_codigo")
    private String compraAgilCodigo;

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @Column(name = "fecha_sync")
    private LocalDateTime fechaSync;
}
