package co.analisys.gimnasio.repository;

import co.analisys.gimnasio.model.Equipo;
import co.analisys.gimnasio.model.EquipoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipoRepository extends JpaRepository<Equipo, EquipoId> {
}
