package cl.zona_ti.compra_service.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.zona_ti.compra_service.Model.LicitacionEntity;

public interface LicitacionRepository extends JpaRepository<LicitacionEntity, String> {

    // Trae la licitacion con sus items ya inicializados (JOIN FETCH) en la
    // misma consulta. Necesario porque listarUltimasOchoHoras() resuelve las
    // candidatas con parallelStream(): el mapeo a DTO (que recorre
    // entity.getItems(), coleccion LAZY) corre en hilos del ForkJoinPool, no
    // en el hilo de la request, asi que ahi no esta disponible la sesion de
    // Hibernate que abre "open-in-view" -- usar findById() + acceder a
    // getItems() despues revienta con LazyInitializationException. Con el
    // JOIN FETCH la coleccion ya viene cargada, sin importar en que hilo se
    // lea despues.
    @Query("SELECT l FROM LicitacionEntity l LEFT JOIN FETCH l.items WHERE l.codigoExterno = :codigo")
    Optional<LicitacionEntity> findByIdConItems(@Param("codigo") String codigo);
}
