package cl.zona_ti.compra_service.Service;

import org.springframework.stereotype.Service;

import cl.zona_ti.compra_service.Client.LicitacionClient;
import cl.zona_ti.compra_service.Dto.LicitacionDto.LicitacionResponse;

@Service
public class LicitacionService {

    private final LicitacionClient licitacionClient;

    public LicitacionService(LicitacionClient licitacionClient) {
        this.licitacionClient = licitacionClient;
    }

    public LicitacionResponse getLicitacionByCodigo(String codigo) {
        return licitacionClient.getLicitacionByCodigo(codigo);
    }

    public LicitacionResponse getLicitacionesPorFecha(String fecha) {
        return licitacionClient.getLicitacionesPorFecha(fecha);
    }
}
