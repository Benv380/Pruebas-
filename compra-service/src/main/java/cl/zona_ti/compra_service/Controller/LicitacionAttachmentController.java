package cl.zona_ti.compra_service.Controller;

import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.zona_ti.compra_service.Dto.AdjuntoLicitacionDto.AdjuntoLicitacionListadoResponse;
import cl.zona_ti.compra_service.Service.LicitacionAttachmentService;

// Mismas rutas relativas que AdjuntoController (Compra Ágil), pero bajo
// /compra/licitacion para que conviva con LicitacionController.
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/compra/licitacion")
public class LicitacionAttachmentController {

    private final LicitacionAttachmentService licitacionAttachmentService;

    public LicitacionAttachmentController(LicitacionAttachmentService licitacionAttachmentService) {
        this.licitacionAttachmentService = licitacionAttachmentService;
    }

    // GET /compra/licitacion/2378-71-LS26/adjuntos
    @GetMapping("/{codigo}/adjuntos")
    public ResponseEntity<AdjuntoLicitacionListadoResponse> listar(@PathVariable String codigo) {
        return ResponseEntity.ok(licitacionAttachmentService.listar(codigo));
    }

    // GET /compra/licitacion/adjuntos/17
    @GetMapping("/adjuntos/{id}")
    public ResponseEntity<byte[]> descargar(@PathVariable Long id) {
        try {
            return licitacionAttachmentService.descargar(id);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
