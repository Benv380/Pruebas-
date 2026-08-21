package cl.zona_ti.compra_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.zona_ti.compra_service.Dto.AdjuntoLicitacionDto.AdjuntoLicitacionArchivo;
import cl.zona_ti.compra_service.Model.AdjuntoLicitacionEntity;

import java.util.List;

public interface AdjuntoLicitacionRepository extends JpaRepository<AdjuntoLicitacionEntity, Long> {

    List<AdjuntoLicitacionEntity> findByCodigoLicitacion(String codigoLicitacion);

    // Proyección liviana para listar: no trae la columna "contenido" (el
    // binario puede pesar varios MB), a diferencia de findByCodigoLicitacion.
    @Query("SELECT new cl.zona_ti.compra_service.Dto.AdjuntoLicitacionDto$AdjuntoLicitacionArchivo("
            + "CAST(e.id AS string), e.nombreArchivo) "
            + "FROM AdjuntoLicitacionEntity e WHERE e.codigoLicitacion = :codigo")
    List<AdjuntoLicitacionArchivo> listarPorCodigo(@Param("codigo") String codigoLicitacion);
}
