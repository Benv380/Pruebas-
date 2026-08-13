package cl.zona_ti.compra_service.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.zona_ti.compra_service.Model.AdjuntoEntity;

public interface AdjuntoRepository extends JpaRepository<AdjuntoEntity, String> {

    List<AdjuntoEntity> findByCompraAgilCodigo(String compraAgilCodigo);
}
