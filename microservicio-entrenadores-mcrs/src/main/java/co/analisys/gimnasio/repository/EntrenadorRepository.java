package co.analisys.gimnasio.repository;

import co.analisys.gimnasio.model.Entrenador;
import co.analisys.gimnasio.model.EntrenadorId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntrenadorRepository extends JpaRepository<Entrenador, EntrenadorId> {
}
