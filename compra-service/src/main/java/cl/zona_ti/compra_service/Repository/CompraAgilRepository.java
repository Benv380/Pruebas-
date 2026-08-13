package cl.zona_ti.compra_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.zona_ti.compra_service.Model.CompraAgilEntity;

public interface CompraAgilRepository extends JpaRepository<CompraAgilEntity, String> {
}
