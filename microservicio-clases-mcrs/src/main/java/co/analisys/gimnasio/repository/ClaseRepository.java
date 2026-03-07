package co.analisys.gimnasio.repository;

import co.analisys.gimnasio.model.Clase;
import co.analisys.gimnasio.model.ClaseId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaseRepository extends JpaRepository<Clase, ClaseId> {
}
