package cl.zona_ti.compra_service.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.zona_ti.compra_service.Dto.AdjuntoDto.AdjuntoListadoResponse;
import cl.zona_ti.compra_service.Service.AdjuntoService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/compra/agil")
public class AdjuntoController {

    private final AdjuntoService adjuntoService;

    public AdjuntoController(AdjuntoService adjuntoService) {
        this.adjuntoService = adjuntoService;
    }

    // GET /compra/agil/1629-68-COT26/adjuntos
    @GetMapping("/{codigo}/adjuntos")
    public ResponseEntity<AdjuntoListadoResponse> listar(@PathVariable String codigo) {
        return ResponseEntity.ok(adjuntoService.listar(codigo));
    }

    // GET /compra/agil/adjuntos/9E9DAC13-3143-4C90-BCB6-A5BCE01A9C22
    @GetMapping("/adjuntos/{uuid}")
    public ResponseEntity<byte[]> descargar(@PathVariable String uuid) {
        return adjuntoService.descargar(uuid);
    }
}