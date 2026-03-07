package co.analisys.gimnasio.service;

import co.analisys.gimnasio.dto.PuedeAsistirClaseResponse;
import co.analisys.gimnasio.model.Email;
import co.analisys.gimnasio.model.Miembro;
import co.analisys.gimnasio.model.MiembroId;
import co.analisys.gimnasio.repository.MiembroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MiembroService {

    @Autowired
    private MiembroRepository miembroRepository;

    public Miembro obtenerMiembro(MiembroId id) {
        return miembroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Miembro no encontrado"));
    }

    public Miembro registrarMiembro(Miembro miembro) {
        return miembroRepository.save(miembro);
    }

    public List<Miembro> obtenerTodosMiembros() {
        return miembroRepository.findAll();
    }

    public void actualizarEmail(MiembroId id, Email nuevoEmail) {
        Miembro miembro = obtenerMiembro(id);
        miembro.actualizarEmail(nuevoEmail);
        miembroRepository.save(miembro);
    }

    /**
     * Indica si el miembro puede asistir a clase (existe y membresía activa).
     * Incluye la razón cuando no puede: NO_EXISTE o MEMBRESIA_INACTIVA.
     */
    public PuedeAsistirClaseResponse puedeAsistirAClase(MiembroId id) {
        return miembroRepository.findById(id)
                .map(m -> m.isMembresiaActiva()
                        ? new PuedeAsistirClaseResponse(true, null)
                        : new PuedeAsistirClaseResponse(false, "MEMBRESIA_INACTIVA"))
                .orElse(new PuedeAsistirClaseResponse(false, "NO_EXISTE"));
    }
}
