package cl.zona_ti.compra_service.Service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import cl.zona_ti.compra_service.Client.AuthClient;
import cl.zona_ti.compra_service.Dto.AuthDto;

@Service
public class TokenCacheService {

    private static final long MARGEN_SEGUNDOS = 60;

    private final AuthClient authClient;

    private volatile String tokenActual;
    private volatile Instant expiraEn = Instant.EPOCH;

    public TokenCacheService(AuthClient authClient) {
        this.authClient = authClient;
    }

    public synchronized String getToken() {
        if (tokenActual == null || Instant.now().isAfter(expiraEn)) {
            refrescar();
        }
        return tokenActual;
    }

    private void refrescar() {
        AuthDto.AuthPublicoPayload payload = authClient.obtenerTokenPublico();
        tokenActual = payload.access_token();
        long segundosValidez = payload.expires_in() != null ? payload.expires_in() : 300;
        expiraEn = Instant.now().plusSeconds(Math.max(segundosValidez - MARGEN_SEGUNDOS, 30));
    }
}