package cl.zona_ti.compra_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.zona_ti.compra_service.Model.AdjuntoLicitacionEntity;

import java.util.List;

public interface AdjuntoLicitacionRepository extends JpaRepository<AdjuntoLicitacionEntity, Long> {

    List<AdjuntoLicitacionEntity> findByCodigoLicitacion(String codigoLicitacion);
}