package cl.zona_ti.compra_service.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.zona_ti.compra_service.Dto.LicitacionDto.LicitacionResponse;
import cl.zona_ti.compra_service.Service.LicitacionService;

@RestController
@CrossOrigin(origins = "http://localhost:5173" )
@RequestMapping("/compra/licitacion")
public class LicitacionController {

    private final LicitacionService licitacionService;

    public LicitacionController(LicitacionService licitacionService) {
        this.licitacionService = licitacionService;
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<LicitacionResponse> getLicitacionByCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(licitacionService.getLicitacionByCodigo(codigo));
    }

    // fecha en formato DDMMAAAA, ej: 04082026
    @GetMapping("/por-fecha")
    public ResponseEntity<LicitacionResponse> getLicitacionesPorFecha(@RequestParam String fecha) {
        return ResponseEntity.ok(licitacionService.getLicitacionesPorFecha(fecha));
    }
}
