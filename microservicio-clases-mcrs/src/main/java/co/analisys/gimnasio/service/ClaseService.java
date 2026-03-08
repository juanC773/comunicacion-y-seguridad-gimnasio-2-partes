package co.analisys.gimnasio.service;

import co.analisys.gimnasio.dto.PuedeAsistirClaseResponse;
import co.analisys.gimnasio.event.CambioHorarioClaseEvent;
import co.analisys.gimnasio.event.EventosGimnasioPublisher;
import co.analisys.gimnasio.event.InscripcionEvent;
import co.analisys.gimnasio.event.InscripcionEventPublisher;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClaseService {

    @Autowired
    private ClaseRepository claseRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private InscripcionEventPublisher inscripcionEventPublisher;

    @Autowired
    private EventosGimnasioPublisher eventosGimnasioPublisher;

    public Clase obtenerClase(ClaseId id) {
        return claseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clase no encontrada"));
    }

    public Clase programarClase(Clase clase) {
        Boolean entrenadorExiste = restTemplate.getForObject(
                "http://entrenador-service/entrenadores/" + clase.getEntrenadorId().getEntrenadorid_value() + "/existe",
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
                "http://miembro-service/miembros/" + miembroId.getMiembroid_value() + "/puede-asistir-clase",
                PuedeAsistirClaseResponse.class);

        if (response == null || !response.isPuedeAsistir()) {
            String mensaje = response != null && "MEMBRESIA_INACTIVA".equals(response.getRazon())
                    ? "No se puede inscribir: la membresía está inactiva."
                    : "El miembro no existe.";
            throw new MiembroNoPuedeAsistirException(mensaje);
        }

        clase.inscribirMiembro(miembroId);
        Clase claseGuardada = claseRepository.save(clase);

        try {
            InscripcionEvent event = InscripcionEvent.of(
                    claseId.getClaseid_value(),
                    claseGuardada.getNombre(),
                    miembroId.getMiembroid_value(),
                    claseGuardada.getHorario()
            );
            inscripcionEventPublisher.publicarNuevaInscripcion(event);
        } catch (Exception e) {
            // La inscripción ya se guardó; no fallar si RabbitMQ no está disponible
            org.slf4j.LoggerFactory.getLogger(ClaseService.class).warn("No se pudo publicar evento de inscripción: {}", e.getMessage());
        }

        return claseGuardada;
    }

    public Clase actualizarHorario(ClaseId claseId, LocalDateTime nuevoHorario) {
        Clase clase = obtenerClase(claseId);
        LocalDateTime horarioAnterior = clase.getHorario();
        clase.actualizarHorario(nuevoHorario);
        Clase claseGuardada = claseRepository.save(clase);

        try {
            CambioHorarioClaseEvent event = CambioHorarioClaseEvent.of(
                    claseId.getClaseid_value(),
                    claseGuardada.getNombre(),
                    horarioAnterior,
                    claseGuardada.getHorario()
            );
            eventosGimnasioPublisher.publicarCambioHorarioClase(event);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ClaseService.class).warn("No se pudo publicar evento de cambio de horario: {}", e.getMessage());
        }

        return claseGuardada;
    }
}
