package co.analisys.gimnasio.service;

import co.analisys.gimnasio.dto.PuedeAsistirClaseResponse;
import co.analisys.gimnasio.exception.EntrenadorNoEncontradoException;
import co.analisys.gimnasio.exception.MiembroNoPuedeAsistirException;
import co.analisys.gimnasio.exception.MiembroYaInscritoException;
import co.analisys.gimnasio.model.Clase;
import co.analisys.gimnasio.model.ClaseId;
import co.analisys.gimnasio.model.MiembroId;
import co.analisys.gimnasio.repository.ClaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ClaseService {

    @Autowired
    private ClaseRepository claseRepository;

    @Autowired
    private RestTemplate restTemplate;

    public Clase obtenerClase(ClaseId id) {
        return claseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clase no encontrada"));
    }

    public Clase programarClase(Clase clase) {
        Boolean entrenadorExiste = restTemplate.getForObject(
                "http://ENTRENADOR-SERVICE/entrenadores/" + clase.getEntrenadorId().getEntrenadorid_value() + "/existe",
                Boolean.class);

        if (entrenadorExiste == null || !entrenadorExiste) {
            throw new EntrenadorNoEncontradoException(clase.getEntrenadorId());
        }

        return claseRepository.save(clase);
    }

    public List<Clase> obtenerTodasClases() {
        return claseRepository.findAll();
    }

    public Clase inscribirMiembro(ClaseId claseId, MiembroId miembroId) {
        Clase clase = obtenerClase(claseId);

        if (clase.getMiembrosInscritos().contains(miembroId)) {
            throw new MiembroYaInscritoException();
        }
        if (clase.getMiembrosInscritos().size() >= clase.getCapacidadMaxima()) {
            throw new IllegalStateException("La clase ha alcanzado su capacidad máxima");
        }

        PuedeAsistirClaseResponse response = restTemplate.getForObject(
                "http://MIEMBRO-SERVICE/miembros/" + miembroId.getMiembroid_value() + "/puede-asistir-clase",
                PuedeAsistirClaseResponse.class);

        if (response == null || !response.isPuedeAsistir()) {
            String mensaje = response != null && "MEMBRESIA_INACTIVA".equals(response.getRazon())
                    ? "No se puede inscribir: la membresía está inactiva."
                    : "El miembro no existe.";
            throw new MiembroNoPuedeAsistirException(mensaje);
        }

        clase.inscribirMiembro(miembroId);
        return claseRepository.save(clase);
    }
}
