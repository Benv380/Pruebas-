package cl.zona_ti.compra_service.Controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.zona_ti.compra_service.Dto.CompraAgilDto.CompraAgilDetalleResponse;
import cl.zona_ti.compra_service.Dto.CompraAgilDto.CompraAgilListadoResponse;
import cl.zona_ti.compra_service.Service.CompraAgilService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/compra/agil")
public class CompraAgilController {

    private final CompraAgilService compraAgilService;

    public CompraAgilController(CompraAgilService compraAgilService) {
        this.compraAgilService = compraAgilService;
    }

    // Listado y busqueda con filtros libres, ej:
    // /compra/agil?publicado_desde=2026-08-01T00:00:00Z&publicado_hasta=2026-08-04T23:59:59Z&tamano_pagina=10
    // /compra/agil?ttl_cambio_ms=3600000
    // /compra/agil?q=materiales%20electricos&region=13&estado=publicada,proveedor_seleccionado
    // Ver doc API Compra Agil v2 seccion 5.1 para el detalle de filtros disponibles.
    @GetMapping
    public ResponseEntity<CompraAgilListadoResponse> listar(@RequestParam Map<String, String> filtros) {
        return ResponseEntity.ok(compraAgilService.listar(filtros));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<CompraAgilDetalleResponse> getDetalleByCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(compraAgilService.getDetalleByCodigo(codigo));
    }

    //Traer las compras de las ultimas 8 horas
    @GetMapping("/listar")
    public ResponseEntity<CompraAgilListadoResponse> listarUltimasOchoHoras() {
        return ResponseEntity.ok(compraAgilService.listarUltimasOchoHoras());
    }

}
